package in.koreatech.koin.admin.acceptance;

import static in.koreatech.koin.domain.community.article.model.Board.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.fixture.BoardFixture;
import in.koreatech.koin.fixture.KoinNoticeFixture;
import in.koreatech.koin.fixture.UserFixture;

@Transactional
@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdminNoticeApiTest extends AcceptanceTest {

    @Autowired
    private KoinNoticeFixture koinNoticeFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private ArticleRepository articleRepository;

    Admin 어드민;
    String 엑세스_토큰;
    List<Board> 코인_게시판_리스트;
    Board 공지사항_게시판;
    Article 캠퍼스팀_공지, 유저팀_공지, 비즈니스팀_공지, 코인_공지;

    @BeforeAll
    void setup() {
        clear();
        어드민 = userFixture.코인_운영자();
        엑세스_토큰 = userFixture.getToken(어드민.getUser());
        코인_게시판_리스트 = boardFixture.코인_게시판_리스트();
        공지사항_게시판 = 코인_게시판_리스트.get(KOIN_NOTICE_BOARD_ID - 1);
        캠퍼스팀_공지 =
            koinNoticeFixture.코인_공지_게시글("[코인 캠퍼스팀] 공지사항 테스트", "<p>내용</p>", 어드민, 공지사항_게시판);
        유저팀_공지 =
            koinNoticeFixture.코인_공지_게시글("[코인 유저팀] 공지사항 테스트", "<p>내용</p>", 어드민, 공지사항_게시판);
        비즈니스팀_공지 =
            koinNoticeFixture.코인_공지_게시글("[코인 비즈니스팀] 공지사항 테스트", "<p>내용</p>", 어드민, 공지사항_게시판);
        코인_공지 =
            koinNoticeFixture.코인_공지_게시글("[KOIN] 공지사항 테스트", "<p>내용</p>", 어드민, 공지사항_게시판);
    }

    @Test
    void 관리자_권한으로_코인_공지사항_게시글을_작성한다() throws Exception{
        String jsonBody = """
        {
            "title": "[코인 캠퍼스팀] 공지사항 테스트",
            "content": "<html><head><title>일반공지사항</title></head></html>"
        }
        """;

        mockMvc.perform(
                post("/admin/notice")
                    .header("Authorization", "Bearer " + 엑세스_토큰)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자_권한으로_삭제_되지_않은_코인_공지사항_목록을_조회한다() throws Exception {
        mockMvc.perform(
                get("/admin/notice")
                    .header("Authorization", "Bearer " + 엑세스_토큰)
                    .param("page", "1")
                    .param("is_deleted", "false")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCount").value(4))
            .andExpect(jsonPath("$.currentCount").value(4))
            .andExpect(jsonPath("$.totalPage").value(1))
            .andExpect(jsonPath("$.currentPage").value(1))
            .andExpect(jsonPath("$.notices.length()").value(4));
    }

    @Test
    void 관리자_권한으로_코인_공지사항_게시글을_단건_조회한다() throws Exception {
        Integer noticeId = 캠퍼스팀_공지.getId();

        mockMvc.perform(
                get("/admin/notice/{id}", noticeId)
                    .header("Authorization", "Bearer " + 엑세스_토큰)
            )
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                {
                    "id": %d,
                    "author": "테스트용_코인운영자",
                    "title": "[코인 캠퍼스팀] 공지사항 테스트",
                    "content": "<p>내용</p>",
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """, noticeId)));
    }

    @Test
    void 관리자_권한으로_코인_공지사항_게시글을_삭제한다() throws Exception {
        Integer noticeId = 캠퍼스팀_공지.getId();

        mockMvc.perform(
                delete("/admin/notice/{id}", noticeId)
                    .header("Authorization", "Bearer " + 엑세스_토큰)
            )
            .andExpect(status().isNoContent());

        Article deleteArticle = articleRepository.getById(noticeId);

        assertSoftly(softly -> {
            softly.assertThat(deleteArticle.getTitle()).isEqualTo("[코인 캠퍼스팀] 공지사항 테스트");
            softly.assertThat(deleteArticle.isDeleted()).isEqualTo(true);
        });
    }

    @Test
    void 관리자_권한으로_코인_공지사항_게시글을_수정한다() throws Exception {
        Integer noticeId = 코인_공지.getId();

        // 코인 공지사항 게시글 수정
        String jsonBody = """
        {
            "title": "[코인 캠퍼스팀] 공지사항 테스트 수정",
            "content": "<html><head><title>일반공지사항 수정</title></head></html>"
        }
        """;

        mockMvc.perform(
                put("/admin/notice/{id}", noticeId)
                    .header("Authorization", "Bearer " + 엑세스_토큰)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isOk());

        Article updateNotice = articleRepository.getById(noticeId);

        assertSoftly(softly -> {
            softly.assertThat(updateNotice.getTitle()).isEqualTo("[코인 캠퍼스팀] 공지사항 테스트 수정");
            softly.assertThat(updateNotice.getContent()).isEqualTo("<html><head><title>일반공지사항 수정</title></head></html>");
        });
    }
}

