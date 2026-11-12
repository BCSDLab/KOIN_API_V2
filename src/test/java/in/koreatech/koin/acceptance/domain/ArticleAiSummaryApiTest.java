package in.koreatech.koin.acceptance.domain;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.transaction.TestTransaction;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.ArticleAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BoardAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryStatus;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryRepository;
import in.koreatech.koin.domain.community.article.repository.BoardRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryProperties;
import in.koreatech.koin.domain.community.article.service.summary.ArticleAiSummaryService;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceReader;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummarySourceSeed;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.translation.service.TranslationAiClient;

@TestPropertySource(properties = {
    "article.ai-summary.enabled=true",
    "upstage.api-key=test-key"
})
class ArticleAiSummaryApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private ArticleAcceptanceFixture articleFixture;

    @Autowired
    private BoardAcceptanceFixture boardFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private ArticleAiSummaryRepository articleAiSummaryRepository;

    @Autowired
    private ArticleSummarySourceReader sourceReader;

    @Autowired
    private ArticleAiSummaryProperties properties;

    @Autowired
    private ArticleAiSummaryService articleAiSummaryService;

    @Autowired
    private BoardRepository boardRepository;

    @MockBean
    private TranslationAiClient translationAiClient;

    private Article article;
    private Student student;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        student = userFixture.준호_학생(department, null);
        Board board = boardFixture.자유게시판();
        article = articleFixture.자유글_1(board, student.getUser());
    }

    @Test
    void 삭제된_게시판의_게시글은_요약_큐_등록_대상에서_제외된다() {
        Board deletedBoard = boardRepository.save(
            Board.builder()
                .name("삭제된게시판")
                .isAnonymous(false)
                .articleCount(0)
                .isDeleted(true)
                .isNotice(false)
                .build()
        );
        Article deletedBoardArticle = articleFixture.자유글_2(deletedBoard, student.getUser());

        articleAiSummaryService.enqueueArticlesWithoutSummary(10);

        assertThat(articleAiSummaryRepository.findByArticleId(deletedBoardArticle.getId())).isEmpty();
        assertThat(articleAiSummaryRepository.findByArticleId(article.getId())).isPresent();

        ArticleAiSummary summary = articleAiSummaryRepository.findByArticleId(article.getId()).orElseThrow();
        article.delete();
        entityManager.flush();
        entityManager.clear();

        assertThat(articleAiSummaryService.claimProcessableSummaries("new-worker", 1)).isEmpty();
        summary = articleAiSummaryRepository.findById(summary.getId()).orElseThrow();
        summary.completeFailure("temporary failure", LocalDateTime.now(clock).minusMinutes(1));
        assertThat(articleAiSummaryService.claimProcessableSummaries("new-worker", 1)).isEmpty();

        summary.markProcessing("old-worker", LocalDateTime.now(clock).minusMinutes(1));
        assertThat(articleAiSummaryService.claimProcessableSummaries("new-worker", 1)).isEmpty();
        assertThat(articleAiSummaryService.getGenerationSeed(summary.getId(), "new-worker")).isEmpty();
        assertThat(summary.isProcessingBy("old-worker")).isTrue();
        assertThat(articleAiSummaryService.getGenerationSeed(summary.getId(), "old-worker")).isEmpty();
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.SKIPPED);
        assertThat(summary.getLockedUntil()).isNull();
        assertThat(articleAiSummaryRepository.countAdminSummariesByStatus()).isEmpty();
        assertThat(articleAiSummaryRepository.countAdminQueue(LocalDateTime.now(clock), 10).getRetryableFailedCount()).isZero();
    }

    @Test
    void 요약이_없으면_원문만_반환하고_생성을_대기시킨다() throws Exception {
        mockMvc.perform(
                get("/articles/{articleId}", article.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("<p>내용</p>"));

        ArticleAiSummary summary = articleAiSummaryRepository.findByArticleId(article.getId()).orElseThrow();
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
    }

    @Test
    void V2에서_요약이_없거나_시도_한도를_소진하면_생성을_대기시키고_중복_조회는_횟수를_유지한다() throws Exception {
        mockMvc.perform(
                get("/v2/articles/{articleId}", article.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("<p>내용</p>"))
            .andExpect(jsonPath("$.ai_summary.status").value("PENDING"))
            .andExpect(jsonPath("$.ai_summary.items").isEmpty());

        ArticleAiSummary summary = articleAiSummaryRepository.findByArticleId(article.getId()).orElseThrow();
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);

        LocalDateTime nextAttemptAt = LocalDateTime.now(clock).plusHours(1);
        for (int attempt = 1; attempt <= 10; attempt++) {
            summary.completeFailure("temporary failure", nextAttemptAt);
        }
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/v2/articles/{articleId}", article.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ai_summary.status").value("PENDING"));

        summary = articleAiSummaryRepository.findByArticleId(article.getId()).orElseThrow();
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.WAIT);
        assertThat(summary.getRetryCount()).isZero();
        assertThat(summary.getNextAttemptAt()).isEqualTo(nextAttemptAt);
        assertThat(articleAiSummaryRepository.findWaitingSummariesForUpdate(LocalDateTime.now(clock), 1)).isEmpty();

        summary.waitForRetry("rate limited", nextAttemptAt);
        mockMvc.perform(get("/v2/articles/{articleId}", article.getId())).andExpect(status().isOk());
        assertThat(summary.getRetryCount()).isEqualTo(1);
        assertThat(summary.getNextAttemptAt()).isEqualTo(nextAttemptAt);

        summary.markProcessing("worker", nextAttemptAt);
        mockMvc.perform(get("/v2/articles/{articleId}", article.getId())).andExpect(status().isOk());
        assertThat(summary.isProcessingBy("worker")).isTrue();
        assertThat(summary.getRetryCount()).isEqualTo(1);

        summary.completeFailure("temporary failure", nextAttemptAt);
        mockMvc.perform(get("/v2/articles/{articleId}", article.getId())).andExpect(status().isOk());
        assertThat(summary.getStatus()).isEqualTo(ArticleAiSummaryStatus.FAILED);
        assertThat(summary.getRetryCount()).isEqualTo(2);
        assertThat(summary.getNextAttemptAt()).isEqualTo(nextAttemptAt);
        assertThat(articleAiSummaryService.claimProcessableSummaries("worker", 1)).isEmpty();

        summary.completeFailure("temporary failure", LocalDateTime.now(clock).minusMinutes(1));
        assertThat(articleAiSummaryService.claimProcessableSummaries("worker", 1)).containsExactly(summary.getId());
    }

    @Test
    void 요약이_성공되어_있으면_content_맨_앞에_요약을_붙인다() throws Exception {
        String content = "<p>내용</p>";
        String fingerprint = sourceReader.createFingerprint(ArticleSummarySourceSeed.from(article, content));
        ArticleAiSummary summary = ArticleAiSummary.waiting(
            article,
            fingerprint,
            article.getUpdatedAt(),
            properties.getModel(),
            properties.getPromptVersion()
        );
        summary.completeSuccess(
            List.of("📅 신청은 5월 20일까지 접수됩니다.", "🎯 재학생을 대상으로 모집합니다."),
            fingerprint,
            article.getUpdatedAt(),
            properties.getModel(),
            properties.getPromptVersion(),
            LocalDateTime.now(clock)
        );
        articleAiSummaryRepository.save(summary);

        var response = mockMvc.perform(
                get("/articles/{articleId}", article.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value(containsString("AI 요약")))
            .andExpect(jsonPath("$.content").value(containsString(">📅</span>")))
            .andExpect(jsonPath("$.content").value(containsString(">신청은 5월 20일까지 접수됩니다.</span>")))
            .andExpect(jsonPath("$.content").value(containsString("<p>내용</p>")))
            .andReturn();

        String sourceVersion = JsonAssertions.convertJsonNode(response).path("translation_source_version").asText();
        when(translationAiClient.translate(Map.of("text_0", "내용"), SupportedLanguage.ENGLISH))
            .thenReturn(Map.of("text_0", "Content"));
        // 번역은 별도 트랜잭션에서 원문을 읽으므로 조회한 게시글을 먼저 커밋한다.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        try {
            mockMvc.perform(post("/v2/articles/{articleId}/translations", article.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"target_language":"en-US","expected_source_version":"%s"}
                        """.formatted(sourceVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.source_version").value(sourceVersion))
                .andExpect(jsonPath("$.data.content").value("<p>Content</p>"));
            mockMvc.perform(get("/v2/articles/{articleId}", article.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.translation_source_version").value(sourceVersion));
        } finally {
            clear();
        }
    }

    @Test
    void V2에서_성공한_요약은_원문과_분리된_항목으로_반환한다() throws Exception {
        String content = "<p>내용</p>";
        String fingerprint = sourceReader.createFingerprint(ArticleSummarySourceSeed.from(article, content));
        ArticleAiSummary summary = ArticleAiSummary.waiting(
            article,
            fingerprint,
            article.getUpdatedAt(),
            properties.getModel(),
            properties.getPromptVersion()
        );
        summary.completeSuccess(
            List.of("📅 신청은 5월 20일까지 접수됩니다.", "🎯 재학생을 대상으로 모집합니다."),
            fingerprint,
            article.getUpdatedAt(),
            properties.getModel(),
            properties.getPromptVersion(),
            LocalDateTime.now(clock)
        );
        articleAiSummaryRepository.save(summary);

        mockMvc.perform(
                get("/v2/articles/{articleId}", article.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value(content))
            .andExpect(jsonPath("$.ai_summary.status").value("SUCCESS"))
            .andExpect(jsonPath("$.ai_summary.items.length()").value(2))
            .andExpect(jsonPath("$.ai_summary.items[0].icon").value("📅"))
            .andExpect(jsonPath("$.ai_summary.items[0].text").value("신청은 5월 20일까지 접수됩니다."))
            .andExpect(jsonPath("$.ai_summary.items[1].icon").value("🎯"))
            .andExpect(jsonPath("$.ai_summary.items[1].text").value("재학생을 대상으로 모집합니다."));
    }
}
