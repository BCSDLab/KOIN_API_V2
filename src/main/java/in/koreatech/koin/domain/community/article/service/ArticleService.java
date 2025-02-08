package in.koreatech.koin.domain.community.article.service;

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
import in.koreatech.koin.domain.community.article.model.SearchLogEvent;
import in.koreatech.koin.domain.community.article.model.redis.ArticleHit;
import in.koreatech.koin.domain.community.article.model.redis.ArticleHitUser;
import in.koreatech.koin.domain.community.article.model.redis.BusNoticeArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitUserRepository;
import in.koreatech.koin.domain.community.article.repository.redis.BusArticleRepository;
import in.koreatech.koin.domain.community.article.repository.redis.HotArticleRepository;
import in.koreatech.koin.domain.community.keyword.model.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ArticleHitRepository articleHitRepository;
    private final HotArticleRepository hotArticleRepository;
    private final ArticleHitUserRepository articleHitUserRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final BusArticleRepository busArticleRepository;
    private final KeywordExtractor keywordExtractor;

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

    public ArticlesResponse getArticles(Integer boardId, Integer page, Integer limit) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        if (boardId == null) {
            Page<Article> articles = articleRepository.findAll(pageRequest);
            return ArticlesResponse.of(articles, criteria);
        }
        if (boardId == NOTICE_BOARD_ID) {
            Page<Article> articles = articleRepository.findAllByIsNoticeIsTrue(pageRequest);
            return ArticlesResponse.of(articles, criteria);
        }
        Page<Article> articles = articleRepository.findAllByBoardId(boardId, pageRequest);
        return ArticlesResponse.of(articles, criteria);
    }

    public List<HotArticleItemResponse> getHotArticles() {
        List<Article> cacheList = hotArticleRepository.getHotArticles(HOT_ARTICLE_LIMIT).stream()
            .map(articleRepository::getById)
            .collect(Collectors.toList());
        if (cacheList.size() < HOT_ARTICLE_LIMIT) {
            List<Article> highestHitArticles = articleRepository.findMostHitArticles(
                LocalDate.now(clock).minusDays(HOT_ARTICLE_BEFORE_DAYS), HOT_ARTICLE_LIMIT);
            cacheList.addAll(highestHitArticles);
            return cacheList.stream().limit(HOT_ARTICLE_LIMIT)
                .map(HotArticleItemResponse::from)
                .toList();
        }
        return cacheList.stream().map(HotArticleItemResponse::from).toList();
    }

    @Transactional
    public ArticlesResponse searchArticles(String query, Integer boardId, Integer page, Integer limit,
        String ipAddress) {
        if (query.length() >= MAXIMUM_SEARCH_LENGTH) {
            throw new KoinIllegalArgumentException("검색어의 최대 길이를 초과했습니다.");
        }

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

        eventPublisher.publishEvent(new SearchLogEvent(query, ipAddress));

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

    @Transactional
    public void updateHotArticles() {
        List<ArticleHit> articleHits = articleHitRepository.findAll();
        articleHitRepository.deleteAll();
        List<Article> allArticles =
            articleRepository.findAllByRegisteredAtIsAfter(LocalDate.now(clock).minusDays(HOT_ARTICLE_BEFORE_DAYS));
        articleHitRepository.saveAll(allArticles.stream().map(ArticleHit::from).toList());

        Map<Integer, Integer> articlesIdWithHit = new HashMap<>();
        for (Article article : allArticles) {
            Optional<ArticleHit> cache = articleHits.stream()
                .filter(articleHit -> articleHit.getId().equals(article.getId()))
                .findAny();
            int beforeArticleHit = cache.isPresent() ? cache.get().getHit() : 0;
            if (article.getTotalHit() - beforeArticleHit <= 0) {
                continue;
            }
            articlesIdWithHit.put(article.getId(), article.getTotalHit() - beforeArticleHit);
        }
        hotArticleRepository.saveArticlesWithHitToRedis(articlesIdWithHit, HOT_ARTICLE_LIMIT);
    }

    @Transactional
    public void updateBusNoticeArticle() {
        List<Article> articles = articleRepository.findBusArticlesTop5OrderByCreatedAtDesc();
        LocalDate latestDate = articles.get(0).getCreatedAt().toLocalDate();
        List<Article> latestArticles = articles.stream()
            .filter(article -> article.getCreatedAt().toLocalDate().isEqual(latestDate))
            .toList();

        if (latestArticles.size() >= 2) {
            latestArticles = latestArticles.stream()
                .sorted((first, second) -> {
                    int firstWeight = 0;
                    int secondWeight = 0;

                    // 제목(title)에 "사과"가 들어가면 후순위, "긴급"이 포함되면 우선순위
                    if (first.getTitle().contains("사과")) firstWeight++;
                    if (first.getTitle().contains("긴급")) firstWeight--;

                    if (second.getTitle().contains("사과")) secondWeight++;
                    if (second.getTitle().contains("긴급")) secondWeight--;

                    return Integer.compare(firstWeight, secondWeight);
                })
                .toList();
        }
        busArticleRepository.save(BusNoticeArticle.from(latestArticles.get(0)));
    }

    public LostItemArticlesResponse getLostItemArticles(Integer page, Integer limit, Integer userId) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles = articleRepository.findAllByBoardId(LOST_ITEM_BOARD_ID, pageRequest);
        return LostItemArticlesResponse.of(articles, criteria, userId);
    }

    public LostItemArticleResponse getLostItemArticle(Integer articleId) {
        Article article = articleRepository.getById(articleId);
        setPrevNextArticle(LOST_ITEM_BOARD_ID, article);
        return LostItemArticleResponse.from(article);
    }

    @Transactional
    public LostItemArticleResponse createLostItemArticle(Integer userId, LostItemArticlesRequest requests) {
        Board lostItemBoard = boardRepository.getById(LOST_ITEM_BOARD_ID);
        List<Article> newArticles = new ArrayList<>();
        User user = userRepository.getById(userId);
        requests.articles()
            .forEach(article -> {
                    Article lostItemArticle = Article.createLostItemArticle(article, lostItemBoard, user);
                    articleRepository.save(lostItemArticle);
                    newArticles.add(lostItemArticle);
                }
            );
        sendKeywordNotification(newArticles);
        return LostItemArticleResponse.from(newArticles.get(0));
    }

    @Transactional
    public void deleteLostItemArticle(Integer articleId) {
        Optional<Article> foundArticle = articleRepository.findById(articleId);
        if (foundArticle.isEmpty()) {
            return;
        }
        foundArticle.get().delete();
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

    private void sendKeywordNotification(List<Article> articles) {
        List<ArticleKeywordEvent> keywordEvents = keywordExtractor.matchKeyword(articles);
        if (!keywordEvents.isEmpty()) {
            for (ArticleKeywordEvent event : keywordEvents) {
                eventPublisher.publishEvent(event);
            }
        }
    }
}
