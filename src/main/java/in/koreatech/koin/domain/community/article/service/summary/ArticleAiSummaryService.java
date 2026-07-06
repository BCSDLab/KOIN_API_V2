package in.koreatech.koin.domain.community.article.service.summary;

import static in.koreatech.koin.domain.community.article.service.ArticleService.LOST_ITEM_BOARD_ID;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleAiSummaryService {

    private final ArticleAiSummaryRepository articleAiSummaryRepository;
    private final ArticleRepository articleRepository;
    private final ArticleSummarySourceReader sourceReader;
    private final ArticleSummaryContentRenderer contentRenderer;
    private final ArticleAiSummaryProperties properties;
    private final UpstageProperties upstageProperties;
    private final Clock clock;

    @Transactional
    public String prependSummaryIfReady(Article article, String content) {
        if (article.getBoard().getId().equals(LOST_ITEM_BOARD_ID)) {
            return content;
        }
        ArticleSummarySourceSeed seed = ArticleSummarySourceSeed.from(article, content);
        String fingerprint = sourceReader.createFingerprint(seed);

        Optional<ArticleAiSummary> optionalSummary = articleAiSummaryRepository.findByArticleId(article.getId());
        if (optionalSummary.isEmpty()) {
            enqueueIfEnabled(article, fingerprint, article.getUpdatedAt());
            return content;
        }

        ArticleAiSummary summary = optionalSummary.get();
        if (summary.isSuccessFor(fingerprint, properties.getModel(), properties.getPromptVersion())) {
            return contentRenderer.prependSummary(content, summary.getSummaryLines());
        }
        if (canGenerate() && !summary.isProcessing() && isStale(summary, fingerprint)) {
            summary.prepareWait(fingerprint, article.getUpdatedAt(), properties.getModel(), properties.getPromptVersion());
        }
        return content;
    }

    @Transactional
    public void enqueueArticlesWithoutSummary(int limit) {
        if (!canGenerate()) {
            return;
        }
        int batchLimit = boundedBatchLimit(limit);
        if (batchLimit <= 0) {
            return;
        }
        List<Integer> articleIds = articleRepository.findArticleIdsWithoutAiSummary(
            LOST_ITEM_BOARD_ID,
            batchLimit
        );
        if (articleIds.isEmpty()) {
            return;
        }
        Map<Integer, Article> articles = articleRepository.findAllByIdInForAiSummary(articleIds)
            .stream()
            .collect(Collectors.toMap(Article::getId, Function.identity(), (first, second) -> first));
        for (Integer articleId : articleIds) {
            Article article = articles.get(articleId);
            if (article == null) {
                continue;
            }
            ArticleSummarySourceSeed seed = ArticleSummarySourceSeed.from(article, article.getContent());
            enqueueIfEnabled(article, sourceReader.createFingerprint(seed), article.getUpdatedAt());
        }
    }

    @Transactional
    public List<Integer> claimProcessableSummaries(String workerId, int limit) {
        if (!canGenerate()) {
            return List.of();
        }
        int batchLimit = boundedBatchLimit(limit);
        if (batchLimit <= 0) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now(clock);
        List<ArticleAiSummary> summaries = new ArrayList<>();
        if (isFailedRetryWindow(now.toLocalTime())) {
            summaries.addAll(articleAiSummaryRepository.findRetryableFailedSummariesForUpdate(
                now,
                batchLimit,
                properties.getMaxRetryCount()
            ));
        }
        int remainingLimit = batchLimit - summaries.size();
        if (remainingLimit > 0) {
            summaries.addAll(articleAiSummaryRepository.findWaitingSummariesForUpdate(now, remainingLimit));
        }
        LocalDateTime lockedUntil = now.plusMinutes(properties.getLockMinutes());
        return summaries.stream()
            .peek(summary -> summary.markProcessing(workerId, lockedUntil))
            .map(ArticleAiSummary::getId)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ArticleSummarySourceSeed> getGenerationSeed(Integer summaryId, String workerId) {
        return articleAiSummaryRepository.findByIdWithArticle(summaryId)
            .filter(summary -> summary.isProcessingBy(workerId))
            .map(summary -> ArticleSummarySourceSeed.from(summary.getArticle(), summary.getArticle().getContent()));
    }

    @Transactional
    public void completeSuccess(Integer summaryId, String workerId, ArticleSummarySource source, List<String> summaryLines) {
        articleAiSummaryRepository.findById(summaryId)
            .filter(summary -> summary.isProcessingBy(workerId))
            .ifPresent(summary -> summary.completeSuccess(
                summaryLines,
                source.fingerprint(),
                source.updatedAt(),
                properties.getModel(),
                properties.getPromptVersion(),
                LocalDateTime.now(clock)
            ));
    }

    @Transactional
    public void completeFailure(Integer summaryId, String workerId, String reason) {
        completeFailure(summaryId, workerId, reason, null);
    }

    @Transactional
    public void completeFailure(Integer summaryId, String workerId, String reason, Duration retryAfter) {
        articleAiSummaryRepository.findById(summaryId)
            .filter(summary -> summary.isProcessingBy(workerId))
            .ifPresent(summary -> {
                int nextRetryCount = summary.getRetryCount() + 1;
                LocalDateTime nextAttemptAt = null;
                if (nextRetryCount < properties.getMaxRetryCount()) {
                    nextAttemptAt = LocalDateTime.now(clock).plus(resolveRetryDelay(nextRetryCount, retryAfter));
                }
                summary.completeFailure(reason, nextAttemptAt);
            });
    }

    @Transactional
    public void completeFailureWithoutRetry(Integer summaryId, String workerId, String reason) {
        articleAiSummaryRepository.findById(summaryId)
            .filter(summary -> summary.isProcessingBy(workerId))
            .ifPresent(summary -> summary.completeFailureWithoutRetry(reason, properties.getMaxRetryCount()));
    }

    @Transactional
    public void skip(Integer summaryId, String workerId, String reason) {
        articleAiSummaryRepository.findById(summaryId)
            .filter(summary -> summary.isProcessingBy(workerId))
            .ifPresent(summary -> summary.skip(reason));
    }

    private void enqueueIfEnabled(Article article, String fingerprint, LocalDateTime sourceUpdatedAt) {
        if (!canGenerate()) {
            return;
        }
        articleAiSummaryRepository.insertWaitIfAbsent(
            article.getId(),
            fingerprint,
            sourceUpdatedAt,
            properties.getModel(),
            properties.getPromptVersion()
        );
    }

    private boolean isStale(ArticleAiSummary summary, String fingerprint) {
        return !Objects.equals(summary.getSourceFingerprint(), fingerprint)
            || !Objects.equals(summary.getPromptVersion(), properties.getPromptVersion())
            || !Objects.equals(summary.getModel(), properties.getModel());
    }

    private boolean canGenerate() {
        return properties.isEnabled() && StringUtils.hasText(upstageProperties.getApiKey());
    }

    private int boundedBatchLimit(int limit) {
        return Math.min(Math.max(limit, 0), properties.getBatchSize());
    }

    private boolean isFailedRetryWindow(LocalTime now) {
        int startHour = properties.getBoundedFailedRetryWindowStartHour();
        int endHour = properties.getBoundedFailedRetryWindowEndHour();
        if (startHour == endHour) {
            return false;
        }
        int currentHour = now.getHour();
        if (startHour < endHour) {
            return currentHour >= startHour && currentHour < endHour;
        }
        return currentHour >= startHour || currentHour < endHour;
    }

    private Duration resolveRetryDelay(int nextRetryCount, Duration retryAfter) {
        Duration maxBackoff = Duration.ofMinutes(properties.getMaxRetryBackoffMinutes());
        if (retryAfter != null && !retryAfter.isNegative() && !retryAfter.isZero()) {
            return retryAfter.compareTo(maxBackoff) > 0 ? maxBackoff : retryAfter;
        }
        long multiplier = 1L << Math.min(Math.max(nextRetryCount - 1, 0), 10);
        Duration configuredBackoff = Duration.ofMinutes((long)properties.getRetryBackoffMinutes() * multiplier);
        return configuredBackoff.compareTo(maxBackoff) > 0 ? maxBackoff : configuredBackoff;
    }
}
