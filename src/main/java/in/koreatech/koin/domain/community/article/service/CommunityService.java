package in.koreatech.koin.domain.community.article.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.ArticleHotKeywordResponse;
import in.koreatech.koin.domain.community.article.exception.ArticleBoardMisMatchException;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeywordIpMap;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchLogRepository;
import in.koreatech.koin.domain.community.article.dto.ArticleResponse;
import in.koreatech.koin.domain.community.article.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.article.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleViewLog;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.BoardTag;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleViewLogRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    public static final int NOTICE_BOARD_ID = 4;
    private static final int HOT_ARTICLE_LIMIT = 10;
    private static final Sort ARTICLES_SORT = Sort.by(Sort.Direction.DESC, "id");

    private final ArticleRepository articleRepository;
    private final ArticleViewLogRepository articleViewLogRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final ArticleSearchLogRepository articleSearchLogRepository;
    private final ArticleSearchKeywordIpMapRepository articleSearchKeywordIpMapRepository;
    private final ArticleSearchKeywordRepository articleSearchKeywordRepository;

    @Transactional
    public ArticleResponse getArticle(Integer userId, Integer boardId, Integer articleId, String ipAddress) {
        Article article = articleRepository.getById(articleId);
        if (isHittable(articleId, userId, ipAddress)) {
            article.increaseHit();
        }
        Board board = getBoard(boardId, article);
        Article prevArticle = articleRepository.getPreviousArticle(board, article);
        Article nextArticle = articleRepository.getNextArticle(board, article);
        article.setPrevNextArticles(prevArticle, nextArticle);
        article.getComment().forEach(comment -> comment.updateAuthority(userId));
        return ArticleResponse.of(article);
    }

    private Board getBoard(Integer boardId, Article article) {
        if (boardId == null) {
            boardId = article.getBoard().getId();
        }
        if (!Objects.equals(boardId, article.getBoard().getId())
            && (!article.getBoard().isNotice() || boardId != NOTICE_BOARD_ID)) {
            throw ArticleBoardMisMatchException.withDetail("boardId: " + boardId + ", articleId: " + article.getId());
        }
        return boardRepository.getById(boardId);
    }

    private boolean isHittable(Integer articleId, Integer userId, String ipAddress) {
        if (userId == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        Optional<ArticleViewLog> foundLog = articleViewLogRepository.findByArticleIdAndUserId(articleId, userId);
        if (foundLog.isEmpty()) {
            articleViewLogRepository.save(
                ArticleViewLog.builder()
                    .article(articleRepository.getById(articleId))
                    .user(userRepository.getById(userId))
                    .ip(ipAddress)
                    .build()
            );
            return true;
        }
        if (now.isAfter(foundLog.get().getExpiredAt())) {
            foundLog.get().updateExpiredTime();
            return true;
        }
        return false;
    }

    public ArticlesResponse getArticles(Integer boardId, Integer page, Integer limit) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        if (boardId == NOTICE_BOARD_ID) {
            Page<Article> articles = articleRepository.findAllByBoardIsNoticeIsTrue(pageRequest);
            return ArticlesResponse.of(articles, criteria);
        }
        Page<Article> articles = articleRepository.findAllByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles, criteria);
    }

    public List<HotArticleItemResponse> getHotArticles() {
        PageRequest pageRequest = PageRequest.of(0, HOT_ARTICLE_LIMIT, ARTICLES_SORT);
        return articleRepository.findAll(pageRequest).stream()
            .sorted(Comparator.comparing(Article::getHit).reversed())
            .map(HotArticleItemResponse::from)
            .toList();
    }

    @Transactional
    public ArticlesResponse searchArticles(String query, Integer boardId, Integer page, Integer limit, String ipAddress) {
        if(query.length() >= 100) {
            throw new KoinIllegalArgumentException("검색어의 최대 길이를 초과했습니다.");
        }

        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles;
        if (boardId == null) {
            articles = articleRepository.findAllByTitleContaining(query, pageRequest);
        } else if (boardId == NOTICE_BOARD_ID) {
            articles = articleRepository.findAllByBoardIsNoticeIsTrueAndTitleContaining(query, pageRequest);
        } else {
            articles = articleRepository.findAllByBoardIdAndTitleContaining(boardId, query, pageRequest);
        }

        saveOrUpdateSearchLog(query, ipAddress);

        return ArticlesResponse.of(articles, criteria);
    }

    public ArticleHotKeywordResponse getArticlesHotKeyword(Integer count) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Pageable pageable = PageRequest.of(0, count);

        List<String> topKeywords = articleSearchKeywordRepository.findTopKeywords(oneWeekAgo, pageable);

        if (topKeywords == null || topKeywords.isEmpty()) {
            topKeywords = articleSearchKeywordRepository.findTopKeywordsByLatest(pageable);
        }

        return ArticleHotKeywordResponse.from(topKeywords);
    }

    private void saveOrUpdateSearchLog(String query, String ipAddress) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String[] keywords = query.split("\\s+");

        for (String keywordStr : keywords) {
            ArticleSearchKeyword keyword = getOrCreateKeyword(keywordStr);
            ArticleSearchKeywordIpMap map = getOrCreateKeywordIpMap(keyword, ipAddress);

            updateKeywordWeight(keyword, map);
        }
    }

    private ArticleSearchKeyword getOrCreateKeyword(String keywordStr) {
        return articleSearchKeywordRepository.findByKeyword(keywordStr)
            .orElseGet(() -> {
                ArticleSearchKeyword newKeyword = ArticleSearchKeyword.builder()
                    .keyword(keywordStr)
                    .weight(1.0)
                    .lastSearchedAt(LocalDateTime.now())
                    .totalSearch(1)
                    .build();
                articleSearchKeywordRepository.save(newKeyword);
                return newKeyword;
            });
    }

    private ArticleSearchKeywordIpMap getOrCreateKeywordIpMap(ArticleSearchKeyword keyword, String ipAddress) {
        return articleSearchKeywordIpMapRepository.findByArticleSearchKeywordAndIpAddress(keyword, ipAddress)
            .orElseGet(() -> {
                ArticleSearchKeywordIpMap newMap = ArticleSearchKeywordIpMap.builder()
                    .articleSearchKeyword(keyword)
                    .ipAddress(ipAddress)
                    .searchCount(1)
                    .build();
                articleSearchKeywordIpMapRepository.save(newMap);
                return newMap;
            });
    }

    private void updateKeywordWeight(ArticleSearchKeyword keyword, ArticleSearchKeywordIpMap map) {
        map.incrementSearchCount();
        double additionalWeight = calculateAdditionalWeight(map.getSearchCount());
        keyword.updateWeight(keyword.getWeight() + additionalWeight);
        articleSearchKeywordRepository.save(keyword);
    }

    private double calculateAdditionalWeight(int searchCount) {
        return (searchCount <= 5) ? 1.0 / Math.pow(2, searchCount - 1) : 1.0 / Math.pow(2, 4);
    }


    private boolean isFullNoticeBoard(Board board) {
        return board.isNotice() && Objects.equals(board.getTag(), BoardTag.공지사항.getTag());
    }

    @Transactional
    public void resetWeightsAndCounts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sixHoursThirtyMinutesAgo = now.minusHours(6).minusMinutes(30);

        List<ArticleSearchKeyword> keywordsToUpdate = articleSearchKeywordRepository.findByCreatedAtBetween(
            sixHoursThirtyMinutesAgo, now);
        List<ArticleSearchKeywordIpMap> ipMapsToUpdate = articleSearchKeywordIpMapRepository.findByCreatedAtBetween(
            sixHoursThirtyMinutesAgo, now);

        for (ArticleSearchKeyword keyword : keywordsToUpdate) {
            keyword.resetWeight();
        }

        for (ArticleSearchKeywordIpMap ipMap : ipMapsToUpdate) {
            ipMap.resetSearchCount();
        }
    }
}
