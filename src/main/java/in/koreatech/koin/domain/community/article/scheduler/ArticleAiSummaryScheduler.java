package in.koreatech.koin.domain.community.article.scheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryWorker;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ArticleAiSummaryScheduler {

    private final ArticleAiSummaryService articleAiSummaryService;
    private final ArticleAiSummaryWorker articleAiSummaryWorker;
    private final UpstageProperties upstageProperties;
    private final ArticleAiSummaryProperties articleAiSummaryProperties;
    private final ThreadPoolTaskExecutor articleSummaryTaskExecutor;

    public ArticleAiSummaryScheduler(
        ArticleAiSummaryService articleAiSummaryService,
        ArticleAiSummaryWorker articleAiSummaryWorker,
        UpstageProperties upstageProperties,
        ArticleAiSummaryProperties articleAiSummaryProperties,
        @Qualifier("articleSummaryTaskExecutor") ThreadPoolTaskExecutor articleSummaryTaskExecutor
    ) {
        this.articleAiSummaryService = articleAiSummaryService;
        this.articleAiSummaryWorker = articleAiSummaryWorker;
        this.upstageProperties = upstageProperties;
        this.articleAiSummaryProperties = articleAiSummaryProperties;
        this.articleSummaryTaskExecutor = articleSummaryTaskExecutor;
    }

    @Scheduled(fixedDelayString = "${article.ai-summary.scheduler-fixed-delay-ms:60000}")
    public void generateArticleAiSummaries() {
        if (!StringUtils.hasText(upstageProperties.getApiKey())) {
            return;
        }
        try {
            int claimLimit = resolveClaimLimit();
            if (claimLimit <= 0) {
                log.debug("게시글 AI 요약 작업 큐가 가득 차 이번 스케줄을 건너뜁니다.");
                return;
            }
            articleAiSummaryService.enqueueArticlesWithoutSummary(claimLimit);
            String workerId = UUID.randomUUID().toString();
            List<Integer> summaryIds = articleAiSummaryService.claimProcessableSummaries(workerId, claimLimit);
            summaryIds.forEach(summaryId -> articleAiSummaryWorker.process(summaryId, workerId));
        } catch (Exception e) {
            log.error("게시글 AI 요약 스케줄러 실행 중 오류가 발생했습니다.", e);
        }
    }

    private int resolveClaimLimit() {
        ThreadPoolExecutor executor = articleSummaryTaskExecutor.getThreadPoolExecutor();
        int idleWorkerCount = Math.max(0, articleSummaryTaskExecutor.getMaxPoolSize() - articleSummaryTaskExecutor.getActiveCount());
        int remainingQueueCapacity = executor.getQueue().remainingCapacity();
        int availableCapacity = idleWorkerCount + remainingQueueCapacity;
        return Math.min(articleAiSummaryProperties.getBatchSize(), availableCapacity);
    }
}
