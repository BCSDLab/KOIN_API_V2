package in.koreatech.koin.domain.community.article.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import in.koreatech.koin.domain.community.article.dto.BusArticleProjection;
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
import in.koreatech.koin.global.ipaddress.IpAddressParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleSyncService {

    private static final int HOT_ARTICLE_BEFORE_DAYS = 30;
    private static final int HOT_ARTICLE_LIMIT = 10;
    private static final String IP_SEARCH_COUNT_PREFIX = "search:count:ip:";
    private static final String KEYWORD_SET = "popular_keywords";

    private final ArticleHitRepository articleHitRepository;
    private final HotArticleRepository hotArticleRepository;
    private final BusArticleRepository busArticleRepository;
    private final ArticleSearchKeywordRepository articleSearchKeywordRepository;
    private final ArticleRepository articleRepository;
    private final Clock clock;
    private final ArticleSearchKeywordIpMapRepository ipMapRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void updateHotArticles() {
        List<ArticleHit> articleHits = articleHitRepository.findAll();
        articleHitRepository.deleteAll();
        List<Article> allArticles =
            articleRepository.findAllByRegisteredAtIsAfterExcludingBoardId(
                LocalDate.now(clock).minusDays(HOT_ARTICLE_BEFORE_DAYS),
                ArticleService.LOST_ITEM_BOARD_ID
            );
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
        List<BusArticleProjection> articles = articleRepository.findBusArticlesTop5OrderByCreatedAtDesc(
            PageRequest.of(0, 5));
        LocalDate latestDate = articles.get(0).createdAt().toLocalDate();
        List<BusArticleProjection> latestArticles = articles.stream()
            .filter(article -> article.createdAt().toLocalDate().isEqual(latestDate))
            .toList();

        if (latestArticles.size() >= 2) {
            latestArticles = latestArticles.stream()
                .sorted((first, second) -> {
                    int firstWeight = 0;
                    int secondWeight = 0;

                    // 제목(title)에 "사과"가 들어가면 후순위, "긴급"이 포함되면 우선순위
                    if (first.title().contains("사과"))
                        firstWeight++;
                    if (first.title().contains("긴급"))
                        firstWeight--;

                    if (second.title().contains("사과"))
                        secondWeight++;
                    if (second.title().contains("긴급"))
                        secondWeight--;

                    return Integer.compare(firstWeight, secondWeight);
                })
                .toList();
        }

        BusArticleProjection latestArticle = latestArticles.get(0);
        busArticleRepository.save(BusNoticeArticle.of(latestArticle.id(), latestArticle.title()));
    }

    @Transactional
    public void synchronizeKeywords() {
        Set<String> synchronizedIpKeys = syncAllIpSearchCounts();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                clearStoredData(synchronizedIpKeys);
            }
        });
    }

    private Set<String> syncAllIpSearchCounts() {
        Set<String> ipKeys = redisTemplate.keys(IP_SEARCH_COUNT_PREFIX + "*");
        if (ipKeys == null || ipKeys.isEmpty()) {
            return Set.of();
        }

        Set<String> synchronizedIpKeys = new HashSet<>();
        int invalidIpCount = 0;
        for (String ipKey : ipKeys) {
            Optional<String> parsedIpAddress = IpAddressParser.parse(ipKey.substring(IP_SEARCH_COUNT_PREFIX.length()));
            if (parsedIpAddress.isEmpty()) {
                invalidIpCount++;
                continue;
            }

            Map<Object, Object> keywordSearchCounts = redisTemplate.opsForHash().entries(ipKey);
            String ipAddress = parsedIpAddress.get();

            for (Map.Entry<Object, Object> entry : keywordSearchCounts.entrySet()) {
                String searchedKeyword = (String)entry.getKey();
                int searchCount = Integer.parseInt(entry.getValue().toString());
                if (searchCount <= 0)
                    continue;

                articleSearchKeywordRepository.findByKeyword(searchedKeyword).ifPresent(keywordEntity -> {
                    ipMapRepository.findByArticleSearchKeywordAndIpAddress(keywordEntity, ipAddress)
                        .ifPresentOrElse(
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
                });
            }
            synchronizedIpKeys.add(ipKey);
        }
        if (invalidIpCount > 0) {
            log.warn("IP 형식을 확인할 수 없는 검색 집계 {}개는 동기화에서 제외하고 Redis에 보존합니다.", invalidIpCount);
        }
        return synchronizedIpKeys;
    }

    private void clearStoredData(Set<String> synchronizedIpKeys) {
        try {
            redisTemplate.delete(KEYWORD_SET);
            log.info("키워드 집계 데이터 초기화 완료");

            if (!synchronizedIpKeys.isEmpty()) {
                redisTemplate.delete(synchronizedIpKeys);
                log.info("동기화한 IP별 검색 통계 {}개 정리 완료", synchronizedIpKeys.size());
            }
        } catch (Exception e) {
            log.error("DB 커밋 후 Redis 검색 집계 정리 중 오류 발생", e);
        }
    }

    @Transactional
    public void resetWeightsAndCounts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = now.minusHours(6).minusMinutes(30);

        List<ArticleSearchKeyword> keywordsToUpdate = articleSearchKeywordRepository.findByUpdatedAtBetween(before, now);
        List<ArticleSearchKeywordIpMap> ipMapsToUpdate = ipMapRepository.findByUpdatedAtBetween(before, now);

        for (ArticleSearchKeyword keyword : keywordsToUpdate) {
            keyword.resetWeight();
        }

        for (ArticleSearchKeywordIpMap ipMap : ipMapsToUpdate) {
            ipMap.resetSearchCount();
        }
    }
}
