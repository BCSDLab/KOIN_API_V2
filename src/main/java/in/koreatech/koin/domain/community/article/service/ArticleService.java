package in.koreatech.koin.domain.community.article.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeywordIpMap;
import in.koreatech.koin.domain.community.article.model.Board;
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
import in.koreatech.koin._common.event.ArticleKeywordEvent;
import in.koreatech.koin.domain.community.util.KeywordExtractor;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.concurrent.ConcurrencyGuard;
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
        saveOrUpdateSearchLog(query, ipAddress);
        return ArticlesResponse.of(articles, criteria, userId);
    }

    @Transactional
    public LostItemArticlesResponse searchLostItemArticles(String query, Integer page, Integer limit,
        String ipAddress, Integer userId) {
        verifyQueryLength(query);
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), NATIVE_ARTICLES_SORT);
        Page<Article> articles = articleRepository.findAllByBoardIdAndTitleContaining(LOST_ITEM_BOARD_ID, query,
            pageRequest);
        saveOrUpdateSearchLog(query, ipAddress);
        return LostItemArticlesResponse.of(articles, criteria, userId);
    }

    private void verifyQueryLength(String query) {
        if (query.length() >= MAXIMUM_SEARCH_LENGTH) {
            throw new KoinIllegalArgumentException("검색어의 최대 길이를 초과했습니다.");
        }
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

    @ConcurrencyGuard(lockName = "searchLog")
    private void saveOrUpdateSearchLog(String query, String ipAddress) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        String[] keywords = query.split("\\s+");

        for (String keywordStr : keywords) {
            ArticleSearchKeyword keyword = articleSearchKeywordRepository.findByKeyword(keywordStr)
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

            ArticleSearchKeywordIpMap map = articleSearchKeywordIpMapRepository.findByArticleSearchKeywordAndIpAddress(
                    keyword, ipAddress)
                .orElseGet(() -> {
                    ArticleSearchKeywordIpMap newMap = ArticleSearchKeywordIpMap.builder()
                        .articleSearchKeyword(keyword)
                        .ipAddress(ipAddress)
                        .searchCount(1)
                        .build();
                    articleSearchKeywordIpMapRepository.save(newMap);
                    return newMap;
                });

            updateKeywordWeightAndCount(keyword, map);
        }
    }

    /*
     * 추가될 가중치를 계산합니다. 검색 횟수(map.getSearchCount())에 따라 가중치를 다르게 부여합니다:
     * - 검색 횟수가 5회 이하인 경우: 1.0 / 2^(검색 횟수 - 1)
     *   예:
     *     첫 번째 검색: 1.0
     *     두 번째 검색: 0.5
     *     세 번째 검색: 0.25
     *     네 번째 검색: 0.125
     *     다섯 번째 검색: 0.0625
     * - 검색 횟수가 5회를 초과하면: 1.0 / 2^4 (즉, 0.0625) 고정 가중치를 부여합니다.
     * - 검색 횟수가 10회를 초과할 경우: 추가 가중치를 부여하지 않습니다.
     */
    private void updateKeywordWeightAndCount(ArticleSearchKeyword keyword, ArticleSearchKeywordIpMap map) {
        map.incrementSearchCount();
        double additionalWeight = 0.0;

        if (map.getSearchCount() <= 5) {
            additionalWeight = 1.0 / Math.pow(2, map.getSearchCount() - 1);
        } else if (map.getSearchCount() <= 10) {
            additionalWeight = 1.0 / Math.pow(2, 4);
        }

        if (map.getSearchCount() <= 10) {
            keyword.updateWeight(keyword.getWeight() + additionalWeight);
        }
        articleSearchKeywordRepository.save(keyword);
    }

    @Transactional
    public void resetWeightsAndCounts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusHours(6).minusMinutes(30);

        List<ArticleSearchKeyword> keywordsToUpdate = articleSearchKeywordRepository.findByUpdatedAtBetween(
            before, now);
        List<ArticleSearchKeywordIpMap> ipMapsToUpdate = articleSearchKeywordIpMapRepository.findByUpdatedAtBetween(
            before, now);

        for (ArticleSearchKeyword keyword : keywordsToUpdate) {
            keyword.resetWeight();
        }

        for (ArticleSearchKeywordIpMap ipMap : ipMapsToUpdate) {
            ipMap.resetSearchCount();
        }
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
                    if (first.getTitle().contains("사과"))
                        firstWeight++;
                    if (first.getTitle().contains("긴급"))
                        firstWeight--;

                    if (second.getTitle().contains("사과"))
                        secondWeight++;
                    if (second.getTitle().contains("긴급"))
                        secondWeight--;

                    return Integer.compare(firstWeight, secondWeight);
                })
                .toList();
        }
        busArticleRepository.save(BusNoticeArticle.from(latestArticles.get(0)));
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
        if (Objects.equals(author.getId(), userId)) {
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
