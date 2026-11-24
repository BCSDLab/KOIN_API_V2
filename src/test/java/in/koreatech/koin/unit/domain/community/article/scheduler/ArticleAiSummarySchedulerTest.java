package in.koreatech.koin.unit.domain.community.article.scheduler;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import in.koreatech.koin.domain.community.article.scheduler.ArticleAiSummaryScheduler;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryWorker;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;

class ArticleAiSummarySchedulerTest {

    @Test
    void 빈_워커만큼만_선점하고_실행중이거나_대기중이면_추가_선점하지_않는다() {
        var service = mock(ArticleAiSummaryService.class);
        var worker = mock(ArticleAiSummaryWorker.class);
        var executor = mock(ThreadPoolTaskExecutor.class);
        var pool = mock(ThreadPoolExecutor.class);
        var queue = new ArrayBlockingQueue<Runnable>(5);
        when(executor.getThreadPoolExecutor()).thenReturn(pool);
        when(pool.getQueue()).thenReturn(queue);
        when(executor.getCorePoolSize()).thenReturn(1);
        when(executor.getActiveCount()).thenReturn(0, 1, 0);
        when(service.claimProcessableSummaries(anyString(), anyInt())).thenReturn(List.of(1));
        var upstage = new UpstageProperties();
        upstage.setApiKey("test-key");
        var scheduler = new ArticleAiSummaryScheduler(service, worker, upstage, new ArticleAiSummaryProperties(), executor);

        scheduler.generateArticleAiSummaries();
        scheduler.generateArticleAiSummaries();
        queue.add(() -> {});
        scheduler.generateArticleAiSummaries();

        verify(service).enqueueArticlesWithoutSummary(1);
        verify(service).claimProcessableSummaries(anyString(), eq(1));
        verify(worker).process(eq(1), anyString());
    }
}
