package in.koreatech.koin.acceptance.domain;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.ArticleAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BoardAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
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
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;

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
    void V2에서_요약이_없으면_원문과_생성_대기_상태를_분리해_반환한다() throws Exception {
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

        mockMvc.perform(
                get("/articles/{articleId}", article.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value(containsString("AI 요약")))
            .andExpect(jsonPath("$.content").value(containsString(">📅</span>")))
            .andExpect(jsonPath("$.content").value(containsString(">신청은 5월 20일까지 접수됩니다.</span>")))
            .andExpect(jsonPath("$.content").value(containsString("<p>내용</p>")));
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
