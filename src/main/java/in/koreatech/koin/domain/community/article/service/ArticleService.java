package in.koreatech.koin.domain.community.article.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.dto.ArticleHotKeywordResponse;
import in.koreatech.koin.domain.community.article.dto.ArticleResponse;
import in.koreatech.koin.domain.community.article.dto.ArticlesResponse;
import in.koreatech.koin.domain.community.article.dto.HotArticleItemResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleResponse;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesRequest;
import in.koreatech.koin.domain.community.article.dto.LostItemArticlesResponse;
import in.koreatech.koin.domain.community.article.exception.ArticleBoardMisMatchException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.redis.ArticleHitUser;
import in.koreatech.koin.domain.community.article.model.redis.PopularKeywordTracker;
import in.koreatech.koin.domain.community.article.model.KeywordRankingManager;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitUserRepository;
import in.koreatech.koin.domain.community.article.repository.redis.HotArticleRepository;
import in.koreatech.koin._common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    public static final int LOST_ITEM_BOARD_ID = 14;
    public static final int NOTICE_BOARD_ID = 4;
    private static final int HOT_ARTICLE_BEFORE_DAYS = 30;
    private static final int HOT_ARTICLE_LIMIT = 10;
    private static final int MAXIMUM_SEARCH_LENGTH = 100;
    private static final Sort ARTICLES_SORT = Sort.by(
        Sort.Order.desc("id")
    );
    private static final Sort NATIVE_ARTICLES_SORT = Sort.by(
        Sort.Order.desc("id")
    );

    private final ApplicationEventPublisher eventPublisher;
    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final ArticleSearchKeywordIpMapRepository articleSearchKeywordIpMapRepository;
    private final ArticleSearchKeywordRepository articleSearchKeywordRepository;
    private final HotArticleRepository hotArticleRepository;
    private final ArticleHitUserRepository articleHitUserRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final KeywordExtractor keywordExtractor;
    private final PopularKeywordTracker popularKeywordTracker;
    private final KeywordRankingManager keywordRankingManager;

    @Transactional
    public ArticleResponse getArticle(Integer boardId, Integer articleId, String publicIp) {
        Article article = articleRepository.getById(articleId);
        if (articleHitUserRepository.findByArticleIdAndPublicIp(articleId, publicIp).isEmpty()) {
            article.increaseKoinHit();
            articleHitUserRepository.save(ArticleHitUser.of(articleId, publicIp));
        }
        setPrevNextArticle(boardId, article);
        return ArticleResponse.of(article);
    }

    public ArticlesResponse getArticles(Integer boardId, Integer page, Integer limit, Integer userId) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        if (boardId == null) {
            Page<Article> articles = articleRepository.findAll(pageRequest);
            return ArticlesResponse.of(articles, criteria, userId);
        }
        if (boardId == NOTICE_BOARD_ID) {
            Page<Article> articles = articleRepository.findAllByIsNoticeIsTrue(pageRequest);
            return ArticlesResponse.of(articles, criteria, userId);
        }
        Page<Article> articles = articleRepository.findAllByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles, criteria, userId);
    }

    public List<HotArticleItemResponse> getHotArticles() {
        List<Integer> hotArticlesIds = hotArticleRepository.getHotArticles(HOT_ARTICLE_LIMIT);
        List<Article> articles = articleRepository.findAllForHotArticlesByIdIn(hotArticlesIds);

        Map<Integer, Article> articleMap = articles.stream()
            .collect(Collectors.toMap(Article::getId, article -> article));

        List<Article> cacheList = new ArrayList<>(hotArticlesIds.stream()
            .map(articleMap::get)
            .filter(Objects::nonNull)
            .toList());

        if (cacheList.size() < HOT_ARTICLE_LIMIT) {
            List<Article> highestHitArticles = articleRepository.findMostHitArticles(
                LocalDate.now(clock).minusDays(HOT_ARTICLE_BEFORE_DAYS),
                PageRequest.of(0, HOT_ARTICLE_LIMIT)
            );
            cacheList.addAll(highestHitArticles);
            return cacheList.stream().limit(HOT_ARTICLE_LIMIT)
                .map(HotArticleItemResponse::from)
                .toList();
        }
        return cacheList.stream().map(HotArticleItemResponse::from).toList();
    }

    @Transactional
    public ArticlesResponse searchArticles(String query, Integer boardId, Integer page, Integer limit,
        String ipAddress, Integer userId) {
        verifyQueryLength(query);
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NATIVE_ARTICLES_SORT);
        Page<Article> articles;
        if (boardId == null) {
            articles = articleRepository.findAllByTitleContaining(query, pageRequest);
        } else if (boardId == NOTICE_BOARD_ID) {
            articles = articleRepository.findAllByIsNoticeIsTrueAndTitleContaining(query, pageRequest);
        } else {
            articles = articleRepository.findAllByBoardIdAndTitleContaining(boardId, query, pageRequest);
        }

        String[] keywords = query.split("\\s+");

        for (String keyword : keywords) {
            popularKeywordTracker.updateKeywordWeight(ipAddress, keyword);
        }

        return ArticlesResponse.of(articles, criteria, userId);
    }

    public ArticleHotKeywordResponse getArticlesHotKeyword(int count) {
        List<String> topKeywords = keywordRankingManager.getTopKeywords(count);
        return ArticleHotKeywordResponse.from(topKeywords);
    }

    @Transactional
    public LostItemArticlesResponse searchLostItemArticles(String query, Integer page, Integer limit,
        String ipAddress, Integer userId) {
        verifyQueryLength(query);
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NATIVE_ARTICLES_SORT);
        Page<Article> articles = articleRepository.findAllByBoardIdAndTitleContaining(LOST_ITEM_BOARD_ID, query,
            pageRequest);

        String[] keywords = query.split("\\s+");

        for (String keyword : keywords) {
            popularKeywordTracker.updateKeywordWeight(ipAddress, keyword);
        }

        return LostItemArticlesResponse.of(articles, criteria, userId);
    }

    private void verifyQueryLength(String query) {
        if (query.length() >= MAXIMUM_SEARCH_LENGTH) {
            throw new KoinIllegalArgumentException("검색어의 최대 길이를 초과했습니다.");
        }
    }

    public LostItemArticlesResponse getLostItemArticles(String type, Integer page, Integer limit, Integer userId) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles;

        if (type == null) {
            articles = articleRepository.findAllByBoardId(LOST_ITEM_BOARD_ID, pageRequest);
        } else {
            articles = articleRepository.findAllByLostItemArticleType(type, pageRequest);
        }

        return LostItemArticlesResponse.of(articles, criteria, userId);
    }

    public LostItemArticleResponse getLostItemArticle(Integer articleId, Integer userId) {
        Article article = articleRepository.getById(articleId);
        setPrevNextArticle(LOST_ITEM_BOARD_ID, article);

        boolean isMine = false;
        User author = article.getLostItemArticle().getAuthor();
        if (author != null && Objects.equals(author.getId(), userId)) {
            isMine = true;
        }

        return LostItemArticleResponse.of(article, isMine);
    }

    @Transactional
    public LostItemArticleResponse createLostItemArticle(Integer userId, LostItemArticlesRequest requests) {
        Board lostItemBoard = boardRepository.getById(LOST_ITEM_BOARD_ID);
        User user = userRepository.getById(userId);
        List<Article> newArticles = new ArrayList<>();

        for (var article : requests.articles()) {
            Article lostItemArticle = Article.createLostItemArticle(article, lostItemBoard, user);
            articleRepository.save(lostItemArticle);
            newArticles.add(lostItemArticle);
        }

        sendKeywordNotification(newArticles, userId);
        return LostItemArticleResponse.of(newArticles.get(0), true);
    }

    @Transactional
    public void deleteLostItemArticle(Integer articleId, Integer userId) {
        Article foundArticle = articleRepository.getById(articleId);
        User author = foundArticle.getLostItemArticle().getAuthor();
        if (!Objects.equals(author.getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        foundArticle.delete();
    }

    private void setPrevNextArticle(Integer boardId, Article article) {
        Article prevArticle;
        Article nextArticle;
        if (boardId != null) {
            Board board = getBoard(boardId, article);
            prevArticle = articleRepository.getPreviousArticle(board, article);
            nextArticle = articleRepository.getNextArticle(board, article);
        } else {
            prevArticle = articleRepository.getPreviousAllArticle(article);
            nextArticle = articleRepository.getNextAllArticle(article);
        }
        article.setPrevNextArticles(prevArticle, nextArticle);
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

    private void sendKeywordNotification(List<Article> articles, Integer authorId) {
        List<ArticleKeywordEvent> keywordEvents = keywordExtractor.matchKeyword(articles, authorId);
        if (!keywordEvents.isEmpty()) {
            for (ArticleKeywordEvent event : keywordEvents) {
                eventPublisher.publishEvent(event);
            }
        }
    }
}
