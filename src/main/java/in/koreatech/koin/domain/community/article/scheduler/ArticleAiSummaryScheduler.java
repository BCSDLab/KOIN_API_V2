package in.koreatech.koin.domain.community.article.scheduler;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryWorker;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleAiSummaryScheduler {

    private final ArticleAiSummaryService articleAiSummaryService;
    private final ArticleAiSummaryWorker articleAiSummaryWorker;
    private final UpstageProperties upstageProperties;

    @Scheduled(fixedDelayString = "${article.ai-summary.scheduler-fixed-delay-ms:60000}")
    public void generateArticleAiSummaries() {
        if (!StringUtils.hasText(upstageProperties.getApiKey())) {
            return;
        }
        try {
            articleAiSummaryService.enqueueArticlesWithoutSummary();
            String workerId = UUID.randomUUID().toString();
            List<Integer> summaryIds = articleAiSummaryService.claimProcessableSummaries(workerId);
            summaryIds.forEach(summaryId -> articleAiSummaryWorker.process(summaryId, workerId));
        } catch (Exception e) {
            log.error("게시글 AI 요약 스케줄러 실행 중 오류가 발생했습니다.", e);
        }
    }
}
