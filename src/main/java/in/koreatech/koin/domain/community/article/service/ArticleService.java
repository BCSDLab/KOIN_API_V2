package in.koreatech.koin.domain.community.article.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import in.koreatech.koin.domain.community.article.exception.ArticleBoardMisMatchException;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeywordIpMap;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.redis.ArticleHit;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitRepository;
import in.koreatech.koin.domain.community.article.repository.redis.HotArticleRepository;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import in.koreatech.koin.global.model.Criteria;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    public static final int NOTICE_BOARD_ID = 4;
    private static final int HOT_ARTICLE_BEFORE_DAYS = 30;
    private static final int HOT_ARTICLE_LIMIT = 10;
    private static final int MAXIMUM_SEARCH_LENGTH = 100;
    private static final Sort ARTICLES_SORT = Sort.by(
        Sort.Order.desc("registeredAt"),
        Sort.Order.desc("id")
    );

    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final ArticleSearchKeywordIpMapRepository articleSearchKeywordIpMapRepository;
    private final ArticleSearchKeywordRepository articleSearchKeywordRepository;
    private final ArticleHitRepository articleHitRepository;
    private final HotArticleRepository hotArticleRepository;
    private final Clock clock;

    @Transactional
    public ArticleResponse getArticle(Integer boardId, Integer articleId) {
        Article article = articleRepository.getById(articleId);
        //TODO: 추후(Device 관련 로직 구현 후) 조회수 증가 제한 로직 부가 필요
        article.increaseKoinHit();
        Board board = getBoard(boardId, article);
        Article prevArticle = articleRepository.getPreviousArticle(board, article);
        Article nextArticle = articleRepository.getNextArticle(board, article);
        article.setPrevNextArticles(prevArticle, nextArticle);
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

    public ArticlesResponse getArticles(Integer boardId, Integer page, Integer limit) {
        Long total = articleRepository.countBy();
        Criteria criteria = Criteria.of(page, limit, total.intValue());
        Board board = boardRepository.getById(boardId);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
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
            List<Article> highestHitArticles = articleRepository.findAllHotArticles(
                LocalDate.now(clock).minusDays(HOT_ARTICLE_BEFORE_DAYS), HOT_ARTICLE_LIMIT);
            cacheList.addAll(highestHitArticles);
            return cacheList.stream().limit(HOT_ARTICLE_LIMIT)
                .map(HotArticleItemResponse::from)
                .toList();
        }
        return cacheList.stream().map(HotArticleItemResponse::from).toList();
    }

    @ConcurrencyGuard(lockName = "searchLog")
    public ArticlesResponse searchArticles(String query, Integer boardId, Integer page, Integer limit,
        String ipAddress) {
        if (query.length() >= MAXIMUM_SEARCH_LENGTH) {
            throw new KoinIllegalArgumentException("검색어의 최대 길이를 초과했습니다.");
        }

        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageRequest = PageRequest.of(criteria.getPage(), criteria.getLimit(), ARTICLES_SORT);
        Page<Article> articles;
        if (boardId == null) {
            articles = articleRepository.findAllByTitleContaining(query, pageRequest);
        } else if (boardId == NOTICE_BOARD_ID) {
            articles = articleRepository.findAllByIsNoticeIsTrueAndTitleContaining(query, pageRequest);
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

    public void saveOrUpdateSearchLog(String query, String ipAddress) {
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

        keyword.updateWeight(keyword.getWeight() + additionalWeight);
        articleSearchKeywordRepository.save(keyword);
    }

    @Transactional
    public void resetWeightsAndCounts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusHours(6).minusMinutes(30);

        List<ArticleSearchKeyword> keywordsToUpdate = articleSearchKeywordRepository.findByCreatedAtBetween(
            before, now);
        List<ArticleSearchKeywordIpMap> ipMapsToUpdate = articleSearchKeywordIpMapRepository.findByCreatedAtBetween(
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
}
