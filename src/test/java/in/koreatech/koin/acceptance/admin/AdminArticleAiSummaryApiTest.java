package in.koreatech.koin.acceptance.admin;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.ArticleAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BoardAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummary;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryLog;
import in.koreatech.koin.domain.community.article.model.ArticleAiSummaryLogType;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryLogRepository;
import in.koreatech.koin.domain.community.article.repository.ArticleAiSummaryRepository;
import in.koreatech.koin.domain.community.article.service.summary.ArticleSummaryFailureType;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;

class AdminArticleAiSummaryApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private BoardAcceptanceFixture boardFixture;

    @Autowired
    private ArticleAcceptanceFixture articleFixture;

    @Autowired
    private ArticleAiSummaryRepository articleAiSummaryRepository;

    @Autowired
    private ArticleAiSummaryLogRepository articleAiSummaryLogRepository;

    private String koinAdminToken;
    private String studentToken;
    private Article failedArticle;
    private ArticleAiSummary failedSummary;
    private String rawFailureReason;

    @BeforeEach
    void setUp() {
        clear();
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);
        Admin admin = userFixture.영희_운영자();
        studentToken = userFixture.getToken(student.getUser());
        koinAdminToken = userFixture.getToken(admin.getUser());
        Board board = boardFixture.자유게시판();
        Article waitingArticle = articleFixture.자유글_1(board, student.getUser());
        failedArticle = articleFixture.자유글_2(board, student.getUser());

        articleAiSummaryRepository.save(ArticleAiSummary.waiting(
            waitingArticle,
            "fingerprint-wait",
            waitingArticle.getUpdatedAt(),
            "solar-pro3",
            "v9"
        ));
        failedSummary = ArticleAiSummary.waiting(
            failedArticle,
            "fingerprint-failed",
            failedArticle.getUpdatedAt(),
            "solar-pro3",
            "v9"
        );
        rawFailureReason = "Upstage 요약 API 호출에 실패했습니다. status=429, "
            + "body={\"url\":\"https://koreatech.in/file.pdf?token=abc\"} "
            + "Authorization: Bearer abc.def up_TESTKEYDOESNOTEXIST1234567890";
        failedSummary.completeFailure(rawFailureReason, LocalDateTime.now(clock).minusMinutes(1));
        articleAiSummaryRepository.save(failedSummary);
        articleAiSummaryLogRepository.save(ArticleAiSummaryLog.of(
            failedSummary,
            ArticleAiSummaryLogType.FAILED,
            ArticleSummaryFailureType.RATE_LIMIT,
            rawFailureReason,
            "worker-test"
        ));
    }

    @Test
    void 승인된_코인_어드민은_super_admin이_아니어도_게시글_AI_요약_큐_상태를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/articles/ai-summaries/overview")
                    .header("Authorization", "Bearer " + koinAdminToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status_counts[0].status").value("WAIT"))
            .andExpect(jsonPath("$.status_counts[0].count").value(1))
            .andExpect(jsonPath("$.status_counts[3].status").value("FAILED"))
            .andExpect(jsonPath("$.status_counts[3].count").value(1))
            .andExpect(jsonPath("$.queue.ready_wait_count").value(1))
            .andExpect(jsonPath("$.queue.retryable_failed_count").value(1))
            .andExpect(jsonPath("$.config.batch_size").value(5));
    }

    @Test
    void 관리자가_실패한_게시글_AI_요약_목록과_마스킹된_오류를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/articles/ai-summaries")
                    .header("Authorization", "Bearer " + koinAdminToken)
                    .param("status", "FAILED")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.summaries[0].summary_id").value(failedSummary.getId()))
            .andExpect(jsonPath("$.summaries[0].article_id").value(failedArticle.getId()))
            .andExpect(jsonPath("$.summaries[0].failure_type").value("RATE_LIMIT"))
            .andExpect(jsonPath("$.summaries[0].failure_message").value(containsString("body=<redacted>")))
            .andExpect(jsonPath("$.summaries[0].failure_message").value(not(containsString("abc.def"))))
            .andExpect(jsonPath("$.summaries[0].failure_message").value(not(containsString("up_TESTKEY"))))
            .andExpect(jsonPath("$.summaries[0].failure_message").value(not(containsString("token=abc"))));
    }

    @Test
    void 관리자가_게시글_AI_요약_상세를_조회해도_원문과_raw_오류는_노출되지_않는다() throws Exception {
        mockMvc.perform(
                get("/admin/articles/ai-summaries/{summaryId}", failedSummary.getId())
                    .header("Authorization", "Bearer " + koinAdminToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.article_title").value(failedArticle.getTitle()))
            .andExpect(jsonPath("$.failure_message").value(containsString("body=<redacted>")))
            .andExpect(jsonPath("$.content").doesNotExist())
            .andExpect(jsonPath("$.prompt").doesNotExist())
            .andExpect(jsonPath("$.parsed_text").doesNotExist());
    }

    @Test
    void 관리자가_게시글_AI_요약_로그를_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/articles/ai-summaries/logs")
                    .header("Authorization", "Bearer " + koinAdminToken)
                    .param("failure_type", "RATE_LIMIT")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total_count").value(1))
            .andExpect(jsonPath("$.logs[0].summary_id").value(failedSummary.getId()))
            .andExpect(jsonPath("$.logs[0].article_id").value(failedArticle.getId()))
            .andExpect(jsonPath("$.logs[0].event_type").value("FAILED"))
            .andExpect(jsonPath("$.logs[0].failure_type").value("RATE_LIMIT"))
            .andExpect(jsonPath("$.logs[0].message").value(containsString("body=<redacted>")))
            .andExpect(jsonPath("$.logs[0].message").value(not(containsString("abc.def"))))
            .andExpect(jsonPath("$.logs[0].message").value(not(containsString("up_TESTKEY"))))
            .andExpect(jsonPath("$.logs[0].message").value(not(containsString("token=abc"))));
    }

    @Test
    void 관리자가_아니면_게시글_AI_요약_큐를_조회할_수_없다() throws Exception {
        mockMvc.perform(
                get("/admin/articles/ai-summaries/overview")
                    .header("Authorization", "Bearer " + studentToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isForbidden());
    }
}
