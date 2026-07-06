package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryContentRenderer;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceReader;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;

@ExtendWith(MockitoExtension.class)
class ArticleAiSummaryServiceTest {

    @Mock
    private ArticleAiSummaryRepository articleAiSummaryRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleSummarySourceReader sourceReader;

    @Mock
    private ArticleSummaryContentRenderer contentRenderer;

    @Test
    void retry_after가_있으면_WAIT_상태로_다음_시도_시간까지_대기한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);
        ArticleAiSummary summary = processingSummary(clock);
        when(articleAiSummaryRepository.findById(1)).thenReturn(Optional.of(summary));

        service.completeFailure(1, "worker", "rate limited", Duration.ofMinutes(1));

        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
        assertThat(summary.getRetryCount()).isEqualTo(1);
        assertThat(summary.getNextAttemptAt()).isEqualTo(LocalDateTime.now(clock).plusMinutes(1));
        assertThat(summary.getLockedUntil()).isNull();
    }

    @Test
    void retry_after가_없으면_설정된_기본_백오프를_사용한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);
        ArticleAiSummary summary = processingSummary(clock);
        when(articleAiSummaryRepository.findById(1)).thenReturn(Optional.of(summary));

        service.completeFailure(1, "worker", "temporary failure", null);

        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.FAILED);
        assertThat(summary.getRetryCount()).isEqualTo(1);
        assertThat(summary.getNextAttemptAt()).isEqualTo(LocalDateTime.now(clock).plusMinutes(5));
    }

    @Test
    void 실패_재처리_시간대가_아니면_FAILED_큐를_claim하지_않는다() {
        Clock clock = clockAt(12);
        ArticleAiSummaryService service = service(clock);
        ArticleAiSummary waitingSummary = waitingSummary(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        when(articleAiSummaryRepository.findWaitingSummariesForUpdate(now, 5))
            .thenReturn(List.of(waitingSummary));

        service.claimProcessableSummaries("worker", 5);

        verify(articleAiSummaryRepository, never()).findRetryableFailedSummariesForUpdate(any(), anyInt(), anyInt());
        verify(articleAiSummaryRepository).findWaitingSummariesForUpdate(now, 5);
        assertThat(waitingSummary.isProcessingBy("worker")).isTrue();
    }

    @Test
    void 실패_재처리_시간대에는_FAILED_큐를_먼저_claim하고_남은_용량만_WAIT_큐에_사용한다() {
        Clock clock = clockAt(1);
        ArticleAiSummaryService service = service(clock);
        ArticleAiSummary failedSummary = failedSummary(clock);
        ArticleAiSummary waitingSummary = waitingSummary(clock);
        LocalDateTime now = LocalDateTime.now(clock);
        when(articleAiSummaryRepository.findRetryableFailedSummariesForUpdate(now, 5, 5))
            .thenReturn(List.of(failedSummary));
        when(articleAiSummaryRepository.findWaitingSummariesForUpdate(now, 4))
            .thenReturn(List.of(waitingSummary));

        service.claimProcessableSummaries("worker", 5);

        verify(articleAiSummaryRepository).findRetryableFailedSummariesForUpdate(now, 5, 5);
        verify(articleAiSummaryRepository).findWaitingSummariesForUpdate(now, 4);
        assertThat(failedSummary.isProcessingBy("worker")).isTrue();
        assertThat(waitingSummary.isProcessingBy("worker")).isTrue();
    }

    private ArticleAiSummaryService service(Clock clock) {
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setRetryBackoffMinutes(5);
        properties.setMaxRetryBackoffMinutes(60);
        properties.setMaxRetryCount(5);
        UpstageProperties upstageProperties = new UpstageProperties();
        upstageProperties.setApiKey("test-api-key");
        return new ArticleAiSummaryService(
            articleAiSummaryRepository,
            articleRepository,
            sourceReader,
            contentRenderer,
            properties,
            upstageProperties,
            clock
        );
    }

    private Clock clockAt(int hour) {
        return Clock.fixed(
            LocalDateTime.of(2026, 6, 1, hour, 0)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant(),
            ZoneId.of("Asia/Seoul")
        );
    }

    private ArticleAiSummary waitingSummary(Clock clock) {
        return ArticleAiSummary.waiting(
            null,
            "fingerprint",
            LocalDateTime.now(clock),
            "solar-pro3",
            "v9"
        );
    }

    private ArticleAiSummary failedSummary(Clock clock) {
        ArticleAiSummary summary = processingSummary(clock);
        summary.completeFailure("temporary failure", LocalDateTime.now(clock).minusMinutes(1));
        return summary;
    }

    private ArticleAiSummary processingSummary(Clock clock) {
        ArticleAiSummary summary = ArticleAiSummary.waiting(
            null,
            "fingerprint",
            LocalDateTime.now(clock),
            "solar-pro3",
            "v9"
        );
        summary.markProcessing("worker", LocalDateTime.now(clock).plusMinutes(30));
        return summary;
    }
}
