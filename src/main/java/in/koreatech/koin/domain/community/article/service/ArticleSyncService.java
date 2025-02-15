package in.koreatech.koin.domain.community.article.service;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeywordIpMap;
import in.koreatech.koin.domain.community.article.model.redis.ArticleHit;
import in.koreatech.koin.domain.community.article.model.redis.BusNoticeArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordIpMapRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitRepository;
import in.koreatech.koin.domain.community.article.repository.redis.BusArticleRepository;
import in.koreatech.koin.domain.community.article.repository.redis.HotArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleSyncService {

    private static final int HOT_ARTICLE_BEFORE_DAYS = 30;
    private static final int HOT_ARTICLE_LIMIT = 10;

    private final ArticleHitRepository articleHitRepository;
    private final HotArticleRepository hotArticleRepository;
    private final BusArticleRepository busArticleRepository;
    private final ArticleSearchKeywordRepository keywordRepository;
    private final ArticleRepository articleRepository;
    private final Clock clock;
    private final ArticleSearchKeywordIpMapRepository ipMapRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IP_SEARCH_COUNT_PREFIX = "search:count:ip:";

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
    public void synchronizeSearchKeywords() {
        Set<TypedTuple<Object>> topKeywords = redisTemplate.opsForZSet()
            .reverseRangeWithScores("popular_keywords", 0, -1);
        LocalDateTime now = LocalDateTime.now();

        if (topKeywords == null || topKeywords.isEmpty()) {
            clearStoredData();
            return;
        }

        for (TypedTuple<Object> tuple : topKeywords) {
            String keyword = (String)tuple.getValue();
            Double weight = tuple.getScore() != null ? tuple.getScore() : 0.0;

            keywordRepository.findByKeyword(keyword).ifPresentOrElse(
                existingKeyword -> {
                    updateKeyword(existingKeyword, weight, now);
                    updateIpSearchCounts(keyword, existingKeyword);
                },
                () -> {
                    ArticleSearchKeyword newKeyword = createKeyword(keyword, weight, now);
                    updateIpSearchCounts(keyword, newKeyword);
                }
            );
        }

        log.info("모든 키워드 및 IP별 검색 횟수 동기화 완료");

        clearStoredData();
    }

    private void updateIpSearchCounts(String keyword, ArticleSearchKeyword keywordEntity) {
        Set<String> ipKeys = redisTemplate.keys(IP_SEARCH_COUNT_PREFIX + "*");

        if (ipKeys == null || ipKeys.isEmpty()) {
            return;
        }

        for (String ipKey : ipKeys) {
            Map<Object, Object> keywordSearchCounts = redisTemplate.opsForHash().entries(ipKey);

            for (Map.Entry<Object, Object> entry : keywordSearchCounts.entrySet()) {
                String searchedKeyword = (String) entry.getKey();
                Integer searchCount = Integer.parseInt(entry.getValue().toString());
                String ipAddress = ipKey.replace(IP_SEARCH_COUNT_PREFIX, "");

                if (searchCount > 0 && searchedKeyword.equals(keyword)) {
                    ipMapRepository.findByArticleSearchKeywordAndIpAddress(keywordEntity, ipAddress).ifPresentOrElse(
                        existingIpMap -> {
                            existingIpMap.incrementSearchCountBy(searchCount);
                            ipMapRepository.save(existingIpMap);
                        },
                        () -> {
                            ArticleSearchKeywordIpMap newIpMap = ArticleSearchKeywordIpMap.builder()
                                .articleSearchKeyword(keywordEntity)
                                .ipAddress(ipAddress)
                                .searchCount(searchCount)
                                .build();
                            ipMapRepository.save(newIpMap);
                        }
                    );
                }
            }

            redisTemplate.delete(ipKey);
        }
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

    @Transactional
    public void updateKeyword(ArticleSearchKeyword keyword, Double weight, LocalDateTime now) {
        keyword.updateWeight(weight);
        keyword.updateLastSearchedAt(now);
        keyword.incrementTotalSearch();
        keywordRepository.save(keyword);
    }

    @Transactional
    public ArticleSearchKeyword createKeyword(String keyword, Double weight, LocalDateTime now) {
        ArticleSearchKeyword newKeyword = ArticleSearchKeyword.builder()
            .keyword(keyword)
            .weight(weight)
            .lastSearchedAt(now)
            .totalSearch(1)
            .build();
        return keywordRepository.save(newKeyword);
    }

    @Transactional
    public void clearStoredData() {
        try {
            // 1. 인기 키워드 ZSet 초기화
            redisTemplate.delete("popular_keywords");
            log.info("키워드 집계 데이터 초기화 완료");

            // 2. IP별 검색 통계 초기화
            Set<String> ipSearchKeys = redisTemplate.keys("search:count:ip:*");
            if (ipSearchKeys != null && !ipSearchKeys.isEmpty()) {
                redisTemplate.delete(ipSearchKeys);
                log.info("IP별 검색 통계 초기화 완료");
            }
        } catch (Exception e) {
            log.error("데이터 초기화 중 오류 발생", e);
        }
    }
}
