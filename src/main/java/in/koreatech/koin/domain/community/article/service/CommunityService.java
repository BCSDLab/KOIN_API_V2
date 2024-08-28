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
import in.koreatech.koin.domain.community.article.model.ArticleSearchLog;
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
import in.koreatech.koin.domain.user.repository.UserRepository;
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

    private void saveOrUpdateSearchLog(String keyword, String ipAddress) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        Optional<ArticleSearchLog> existingLog = articleSearchLogRepository.findByKeywordAndIpAddress(keyword, ipAddress);

        if (existingLog.isPresent()) {
            articleSearchLogRepository.save(existingLog.get());
        } else {
            ArticleSearchLog newLog = ArticleSearchLog.builder()
                .keyword(keyword)
                .ipAddress(ipAddress)
                .build();
            articleSearchLogRepository.save(newLog);
        }
    }

    private boolean isFullNoticeBoard(Board board) {
        return board.isNotice() && Objects.equals(board.getTag(), BoardTag.공지사항.getTag());
    }


    public ArticleHotKeywordResponse getArticlesHotKeyword(Integer count) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        Pageable pageable = PageRequest.of(0, count);
        List<String> topKeywords = articleSearchLogRepository.findTopKeywords(oneDayAgo, pageable);

        if (topKeywords == null || topKeywords.isEmpty()) {
            topKeywords = articleSearchLogRepository.findTopKeywordsByLatest(pageable);
        }

        return ArticleHotKeywordResponse.from(topKeywords);
    }
}
