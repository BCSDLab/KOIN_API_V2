package in.koreatech.koin.unit.domain.community.article.service.summary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryWorker;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryAiClient;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryExternalApiException;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryIcon;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryItem;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPrompt;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryPromptBuilder;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryResult;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySource;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceReader;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceSeed;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryValidator;

@ExtendWith(MockitoExtension.class)
class ArticleAiSummaryWorkerTest {

    @Mock
    private ArticleAiSummaryService articleAiSummaryService;

    @Mock
    private ArticleSummarySourceReader sourceReader;

    @Mock
    private ArticleSummaryAiClient articleSummaryAiClient;

    private ArticleAiSummaryWorker worker;

    @BeforeEach
    void setUp() {
        ArticleAiSummaryProperties properties = new ArticleAiSummaryProperties();
        properties.setMaxRefinementRetryCount(1);
        worker = new ArticleAiSummaryWorker(
            articleAiSummaryService,
            sourceReader,
            new ArticleSummaryPromptBuilder(),
            articleSummaryAiClient,
            new ArticleSummaryValidator(),
            properties
        );
    }

    @Test
    void 요약_후보가_4개면_핵심만_재선별해_저장한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(tooManyResult(), refinedResult());

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(2)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeSuccess(
            eq(1),
            eq("worker"),
            eq(source),
            eq(List.of(
                "📅 신청 기간: 5월 20일까지",
                "🎯 대상: 재학생",
                "💰 혜택: 50만원 지급"
            ))
        );
        verify(articleAiSummaryService, never()).completeFailure(any(), any(), any());
    }

    @Test
    void 재선별_횟수를_넘어도_4개면_재시도_대기로_전환한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(tooManyResult(), tooManyResult());

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(2)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeFailure(eq(1), eq("worker"), contains("최대 3개"));
        verify(articleAiSummaryService, never()).completeSuccess(any(), any(), any(), any());
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
    }

    @Test
    void 재선별_결과가_비어있으면_일시_실패로_처리해_재시도_가능하게_한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(tooManyResult(), new ArticleSummaryResult(List.of()));

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(2)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeFailure(eq(1), eq("worker"), contains("재선별 결과"));
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
        verify(articleAiSummaryService, never()).completeSuccess(any(), any(), any(), any());
    }

    @Test
    void 최종_검증에_실패하면_한_번_더_재작성해_저장한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(tooLongResult(), refinedResult());

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(2)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeSuccess(
            eq(1),
            eq("worker"),
            eq(source),
            eq(List.of(
                "📅 신청 기간: 5월 20일까지",
                "🎯 대상: 재학생",
                "💰 혜택: 50만원 지급"
            ))
        );
        verify(articleAiSummaryService, never()).completeFailure(any(), any(), any());
    }

    @Test
    void 검증_재작성_후에도_일부_항목만_실패하면_정상_항목만_저장한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(invalidDateResult(), partiallyInvalidResult());

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(2)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeSuccess(
            eq(1),
            eq("worker"),
            eq(source),
            eq(List.of(
                "🎯 대상: 재학생",
                "💰 혜택: 50만원 지급"
            ))
        );
        verify(articleAiSummaryService, never()).completeFailure(any(), any(), any());
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
    }

    @Test
    void 재선별_이후_검증에_실패해도_한_번_더_재작성해_저장한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(tooManyResult(), tooLongResult(), refinedResult());

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(3)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeSuccess(
            eq(1),
            eq("worker"),
            eq(source),
            eq(List.of(
                "📅 신청 기간: 5월 20일까지",
                "🎯 대상: 재학생",
                "💰 혜택: 50만원 지급"
            ))
        );
        verify(articleAiSummaryService, never()).completeFailure(any(), any(), any());
    }

    @Test
    void 검증_재작성_후에도_실패하면_재시도_대기로_전환한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenReturn(tooLongResult(), tooLongResult());

        worker.process(1, "worker");

        verify(articleSummaryAiClient, times(2)).summarize(any(ArticleSummaryPrompt.class));
        verify(articleAiSummaryService).completeFailure(eq(1), eq("worker"), contains("200자"));
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
        verify(articleAiSummaryService, never()).completeSuccess(any(), any(), any(), any());
    }

    @Test
    void 첨부_중심_게시글의_문서_파싱이_일시_실패하면_본문만으로_성공시키지_않고_재시도한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "첨부파일을 확인하세요.",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            true,
            "fingerprint"
        );
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);

        worker.process(1, "worker");

        verify(articleAiSummaryService).completeFailure(eq(1), eq("worker"), contains("첨부 중심 게시글"), isNull());
        verify(articleSummaryAiClient, never()).summarize(any());
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
        verify(articleAiSummaryService, never()).completeSuccess(any(), any(), any(), any());
    }

    @Test
    void retry_after가_있는_외부_API_일시_오류는_재시도_시간과_함께_실패_처리한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        Duration retryAfter = Duration.ofSeconds(30);
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenThrow(new ArticleSummaryExternalApiException("rate limited", true, retryAfter));

        worker.process(1, "worker");

        verify(articleAiSummaryService).completeFailure(eq(1), eq("worker"), eq("rate limited"), eq(retryAfter));
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
        verify(articleAiSummaryService, never()).completeSuccess(any(), any(), any(), any());
    }

    @Test
    void 재시도_불가능한_외부_API_오류는_즉시_재시도_종료_상태로_처리한다() {
        ArticleSummarySourceSeed seed = sourceSeed();
        ArticleSummarySource source = source();
        when(articleAiSummaryService.getGenerationSeed(1, "worker")).thenReturn(Optional.of(seed));
        when(sourceReader.read(seed)).thenReturn(source);
        when(articleSummaryAiClient.summarize(any(ArticleSummaryPrompt.class)))
            .thenThrow(new ArticleSummaryExternalApiException("bad request", false, null));

        worker.process(1, "worker");

        verify(articleAiSummaryService).completeFailureWithoutRetry(eq(1), eq("worker"), eq("bad request"));
        verify(articleAiSummaryService, never()).skip(any(), any(), any());
        verify(articleAiSummaryService, never()).completeSuccess(any(), any(), any(), any());
    }


    private ArticleSummarySourceSeed sourceSeed() {
        return new ArticleSummarySourceSeed(
            1,
            "장학금 신청 안내",
            "신청 기간: 5월 20일까지\n대상: 재학생\n혜택: 50만원 지급\n신청 방법: 온라인 제출",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of()
        );
    }

    private ArticleSummarySource source() {
        return new ArticleSummarySource(
            1,
            "장학금 신청 안내",
            "신청 기간: 5월 20일까지\n대상: 재학생\n혜택: 50만원 지급\n신청 방법: 온라인 제출",
            "학생처",
            LocalDate.of(2026, 5, 1),
            LocalDateTime.of(2026, 5, 1, 10, 0),
            List.of(),
            false,
            "fingerprint"
        );
    }

    private ArticleSummaryResult tooManyResult() {
        return new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청 기간: 5월 20일까지"),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "대상: 재학생"),
            new ArticleSummaryItem(ArticleSummaryIcon.MONEY, "혜택: 50만원 지급"),
            new ArticleSummaryItem(ArticleSummaryIcon.ACTION, "신청 방법: 온라인 제출")
        ));
    }

    private ArticleSummaryResult refinedResult() {
        return new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청 기간: 5월 20일까지"),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "대상: 재학생"),
            new ArticleSummaryItem(ArticleSummaryIcon.MONEY, "혜택: 50만원 지급")
        ));
    }

    private ArticleSummaryResult invalidDateResult() {
        return new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청 기간: 5월 21일까지")
        ));
    }

    private ArticleSummaryResult partiallyInvalidResult() {
        return new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.CALENDAR, "신청 기간: 5월 21일까지"),
            new ArticleSummaryItem(ArticleSummaryIcon.TARGET, "대상: 재학생"),
            new ArticleSummaryItem(ArticleSummaryIcon.MONEY, "혜택: 50만원 지급")
        ));
    }

    private ArticleSummaryResult tooLongResult() {
        return new ArticleSummaryResult(List.of(
            new ArticleSummaryItem(ArticleSummaryIcon.DEFAULT, "가".repeat(201))
        ));
    }
}
