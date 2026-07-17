package in.koreatech.koin.admin.article.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.admin.article.dto.AdminArticleAiSummariesResponse;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryOverviewResponse;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryProjection;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryQueueCountProjection;
import in.koreatech.koin.admin.article.dto.AdminArticleAiSummaryResponse;
import in.koreatech.koin.admin.article.exception.AdminArticleAiSummaryNotFoundException;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureReasonSanitizer;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminArticleAiSummaryService {

    private final ArticleAiSummaryRepository articleAiSummaryRepository;
    private final ArticleAiSummaryProperties properties;
    private final ArticleSummaryFailureReasonSanitizer failureReasonSanitizer;
    private final Clock clock;

    @Value("${article.ai-summary.scheduler-fixed-delay-ms:60000}")
    private Long schedulerFixedDelayMs;

    public AdminArticleAiSummaryOverviewResponse getOverview() {
        Map<String, Long> statusCounts = articleAiSummaryRepository.countAdminSummariesByStatus().stream()
            .collect(Collectors.toMap(
                count -> count.getStatus(),
                count -> nullToZero(count.getSummaryCount()),
                (first, second) -> first
            ));
        AdminArticleAiSummaryQueueCountProjection queue = articleAiSummaryRepository.countAdminQueue(
            LocalDateTime.now(clock),
            properties.getMaxRetryCount()
        );

        return new AdminArticleAiSummaryOverviewResponse(
            Arrays.stream(ArticleAiSummaryStatus.values())
                .map(status -> new AdminArticleAiSummaryOverviewResponse.StatusCountResponse(
                    status.name(),
                    statusCounts.getOrDefault(status.name(), 0L)
                ))
                .toList(),
            new AdminArticleAiSummaryOverviewResponse.QueueResponse(
                nullToZero(queue.getReadyWaitCount()),
                nullToZero(queue.getDelayedWaitCount()),
                nullToZero(queue.getProcessingCount()),
                nullToZero(queue.getExpiredProcessingCount()),
                nullToZero(queue.getRetryableFailedCount())
            ),
            new AdminArticleAiSummaryOverviewResponse.ConfigResponse(
                schedulerFixedDelayMs,
                properties.getBatchSize(),
                properties.getMaxRetryCount(),
                properties.getBoundedFailedRetryWindowStartHour(),
                properties.getBoundedFailedRetryWindowEndHour()
            )
        );
    }

    public AdminArticleAiSummariesResponse getSummaries(Integer page, Integer limit, ArticleAiSummaryStatus status) {
        Criteria criteria = Criteria.of(page, limit);
        PageRequest pageable = PageRequest.of(criteria.getPage(), criteria.getLimit());
        Page<AdminArticleAiSummaryResponse> summaries = articleAiSummaryRepository
            .findAdminSummaries(status == null ? null : status.name(), pageable)
            .map(this::toResponse);
        return AdminArticleAiSummariesResponse.of(summaries, criteria);
    }

    public AdminArticleAiSummaryResponse getSummary(Integer summaryId) {
        return articleAiSummaryRepository.findAdminSummaryById(summaryId)
            .map(this::toResponse)
            .orElseThrow(() -> AdminArticleAiSummaryNotFoundException.withDetail("summaryId: " + summaryId));
    }

    private AdminArticleAiSummaryResponse toResponse(AdminArticleAiSummaryProjection summary) {
        String failureMessage = failureReasonSanitizer.sanitize(summary.getFailureReason());
        return new AdminArticleAiSummaryResponse(
            summary.getSummaryId(),
            summary.getArticleId(),
            summary.getBoardId(),
            summary.getArticleTitle(),
            summary.getStatus(),
            failureReasonSanitizer.classify(failureMessage),
            failureMessage,
            summary.getRetryCount(),
            summary.getNextAttemptAt(),
            summary.getLockedUntil(),
            summary.getWorkerId(),
            summary.getCreatedAt(),
            summary.getUpdatedAt(),
            summary.getSummarizedAt(),
            summary.getSourceUpdatedAt(),
            summary.getModel(),
            summary.getPromptVersion()
        );
    }

    private Long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
