package in.koreatech.koin.domain.community.article.service;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleSearchKeyword;
import in.koreatech.koin.domain.community.article.model.redis.ArticleHit;
import in.koreatech.koin.domain.community.article.model.redis.BusNoticeArticle;
import in.koreatech.koin.domain.community.article.model.redis.RedisKeywordTracker;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleSearchKeywordRepository;
import in.koreatech.koin.domain.community.article.repository.redis.ArticleHitRepository;
import in.koreatech.koin.domain.community.article.repository.redis.BusArticleRepository;
import in.koreatech.koin.domain.community.article.repository.redis.HotArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RedisKeywordTracker redisKeywordTracker;
    private final ArticleSearchKeywordRepository keywordRepository;
    private final ArticleRepository articleRepository;
    private final Clock clock;

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

    public void syncKeywordsFromRedisToDatabase() {
        Set<Object> redisTopKeywords = redisKeywordTracker.getTopKeywords(100);
        LocalDateTime now = LocalDateTime.now();

        for (Object keywordObj : redisTopKeywords) {
            String keyword = (String) keywordObj;
            Double redisWeight = Optional.ofNullable(redisKeywordTracker.getCurrentWeight(keyword)).orElse(0.0);

            keywordRepository.findByKeyword(keyword).ifPresentOrElse(
                existingKeyword -> updateExistingKeyword(existingKeyword, redisWeight, now),
                () -> createNewKeyword(keyword, redisWeight, now)
            );
        }
        log.info("Redis에서 MySQL로 키워드 동기화 완료");
    }

    @Transactional
    public void updateExistingKeyword(ArticleSearchKeyword keyword, Double redisWeight, LocalDateTime now) {
        keyword.updateWeight(redisWeight);
        keyword.updateLastSearchedAt(now);
        keyword.incrementTotalSearch();
        keywordRepository.save(keyword);
    }

    @Transactional
    public void createNewKeyword(String keyword, Double redisWeight, LocalDateTime now) {
        ArticleSearchKeyword newKeyword = ArticleSearchKeyword.builder()
            .keyword(keyword)
            .weight(redisWeight)
            .lastSearchedAt(now)
            .totalSearch(1)
            .build();
        keywordRepository.save(newKeyword);
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
}
