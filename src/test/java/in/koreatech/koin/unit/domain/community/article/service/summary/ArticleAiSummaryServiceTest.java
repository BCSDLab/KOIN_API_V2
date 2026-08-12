package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryLog;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryLogType;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryLogRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryContentRenderer;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureReasonSanitizer;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceReader;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryView;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;

@ExtendWith(MockitoExtension.class)
class ArticleAiSummaryServiceTest {

    @Mock
    private ArticleAiSummaryRepository articleAiSummaryRepository;

    @Mock
    private ArticleAiSummaryLogRepository articleAiSummaryLogRepository;

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

        ArgumentCaptor<ArticleAiSummaryLog> logCaptor = ArgumentCaptor.forClass(ArticleAiSummaryLog.class);
        verify(articleAiSummaryLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getEventType()).isEqualTo(ArticleAiSummaryLogType.RETRY_WAITING);
        assertThat(logCaptor.getValue().getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
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
    void 모델이_바뀌어도_원문이_같으면_기존_요약을_새_디자인으로_반환하고_재생성을_대기시킨다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);
        Article article = mock(Article.class);
        Board board = mock(Board.class);
        ArticleAiSummary summary = completedSummary(clock, article, "solar-open2", "v10");
        when(board.getId()).thenReturn(1);
        when(article.getBoard()).thenReturn(board);
        when(article.getId()).thenReturn(1);
        when(article.getUpdatedAt()).thenReturn(LocalDateTime.now(clock));
        when(sourceReader.createFingerprint(any())).thenReturn("fingerprint");
        when(articleAiSummaryRepository.findByArticleId(1)).thenReturn(Optional.of(summary));
        when(contentRenderer.prependSummary(eq("본문"), eq(List.of("기존 요약입니다."))))
            .thenReturn("새 디자인 요약\n본문");

        String content = service.prependSummaryIfReady(article, "본문");

        assertThat(content).isEqualTo("새 디자인 요약\n본문");
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
        assertThat(summary.getSummaryLines()).containsExactly("기존 요약입니다.");
        assertThat(summary.getModel()).isEqualTo("solar-pro4");
        assertThat(summary.getPromptVersion()).isEqualTo("v11");
        verify(contentRenderer).prependSummary("본문", List.of("기존 요약입니다."));
    }

    @Test
    void V2에서_모델이_바뀌어도_원문이_같으면_기존_요약을_반환한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);
        Article article = mock(Article.class);
        Board board = mock(Board.class);
        ArticleAiSummary summary = completedSummary(clock, article, "solar-open2", "v10");
        when(board.getId()).thenReturn(1);
        when(article.getBoard()).thenReturn(board);
        when(article.getId()).thenReturn(1);
        when(article.getUpdatedAt()).thenReturn(LocalDateTime.now(clock));
        when(sourceReader.createFingerprint(any())).thenReturn("fingerprint");
        when(articleAiSummaryRepository.findByArticleId(1)).thenReturn(Optional.of(summary));

        ArticleSummaryView summaryView = service.getSummary(article, "본문");

        assertThat(summaryView.status()).isEqualTo(ArticleSummaryView.Status.SUCCESS);
        assertThat(summaryView.summaryLines()).containsExactly("기존 요약입니다.");
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
        verify(contentRenderer, never()).prependSummary(any(), any());
    }

    @Test
    void V2에서_요약이_없으면_PENDING을_반환하고_WAIT으로_등록한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);
        Article article = mock(Article.class);
        Board board = mock(Board.class);
        LocalDateTime updatedAt = LocalDateTime.now(clock);
        when(board.getId()).thenReturn(1);
        when(article.getBoard()).thenReturn(board);
        when(article.getId()).thenReturn(1);
        when(article.getUpdatedAt()).thenReturn(updatedAt);
        when(sourceReader.createFingerprint(any())).thenReturn("fingerprint");
        when(articleAiSummaryRepository.findByArticleId(1)).thenReturn(Optional.empty());

        ArticleSummaryView summaryView = service.getSummary(article, "본문");

        assertThat(summaryView.status()).isEqualTo(ArticleSummaryView.Status.PENDING);
        assertThat(summaryView.summaryLines()).isEmpty();
        verify(articleAiSummaryRepository).insertWaitIfAbsent(
            1,
            "fingerprint",
            updatedAt,
            "solar-pro4",
            "v11"
        );
    }

    @Test
    void V2에서_원문이_바뀌면_기존_요약을_제외하고_PENDING을_반환한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);
        Article article = mock(Article.class);
        Board board = mock(Board.class);
        ArticleAiSummary summary = completedSummary(clock, article, "solar-pro4", "v11");
        when(board.getId()).thenReturn(1);
        when(article.getBoard()).thenReturn(board);
        when(article.getId()).thenReturn(1);
        when(article.getUpdatedAt()).thenReturn(LocalDateTime.now(clock));
        when(sourceReader.createFingerprint(any())).thenReturn("changed-fingerprint");
        when(articleAiSummaryRepository.findByArticleId(1)).thenReturn(Optional.of(summary));

        ArticleSummaryView summaryView = service.getSummary(article, "변경된 본문");

        assertThat(summaryView.status()).isEqualTo(ArticleSummaryView.Status.PENDING);
        assertThat(summaryView.summaryLines()).isEmpty();
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
        assertThat(summary.getSummaryLines()).isEmpty();
    }

    @Test
    void 게시글_AI_요약_로그는_90일_이전_데이터를_삭제한다() {
        Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        ArticleAiSummaryService service = service(clock);

        service.deleteOldLogs();

        verify(articleAiSummaryLogRepository).deleteOlderThan(LocalDateTime.now(clock).minusDays(90));
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
        properties.setEnabled(true);
        properties.setRetryBackoffMinutes(5);
        properties.setMaxRetryBackoffMinutes(60);
        properties.setMaxRetryCount(5);
        UpstageProperties upstageProperties = new UpstageProperties();
        upstageProperties.setApiKey("test-api-key");
        return new ArticleAiSummaryService(
            articleAiSummaryRepository,
            articleAiSummaryLogRepository,
            articleRepository,
            sourceReader,
            contentRenderer,
            new ArticleSummaryFailureReasonSanitizer(),
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

    private ArticleAiSummary completedSummary(
        Clock clock,
        Article article,
        String model,
        String promptVersion
    ) {
        ArticleAiSummary summary = ArticleAiSummary.waiting(
            article,
            "fingerprint",
            LocalDateTime.now(clock),
            model,
            promptVersion
        );
        summary.markProcessing("worker", LocalDateTime.now(clock).plusMinutes(30));
        summary.completeSuccess(
            List.of("기존 요약입니다."),
            "fingerprint",
            LocalDateTime.now(clock),
            model,
            promptVersion,
            LocalDateTime.now(clock)
        );
        return summary;
    }
}
