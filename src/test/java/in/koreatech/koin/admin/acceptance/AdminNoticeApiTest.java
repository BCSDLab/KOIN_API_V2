package in.koreatech.koin.admin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.KoinArticle;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.ArticleFixture;
import in.koreatech.koin.fixture.BoardFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class AdminNoticeApiTest extends AcceptanceTest {

    @Autowired
    private ArticleFixture articleFixture;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private BoardFixture boardFixture;

    @Autowired
    private ArticleRepository articleRepository;

    User adminUser;
    Board boardId1, boardId2, boardId3, boardId4, boardId5, boardId6, boardId7, boardId8, boardId9;
    Article article1, article2, article3, article4;

    @BeforeAll
    void setup() {
        clear();
        adminUser = userFixture.코인_운영자();
        boardId1 = boardFixture.자유게시판();
        boardId2 = boardFixture.취업게시판();
        boardId3 = boardFixture.익명게시판();
        boardId4 = boardFixture.공지사항();
        boardId5 = boardFixture.일반공지();
        boardId6 = boardFixture.장학공지();
        boardId7 = boardFixture.학사공지();
        boardId8 = boardFixture.취업공지();
        boardId9 = boardFixture.코인공지();

        article1 =
            articleFixture.코인_공지_게시글("[캠퍼스팀] 공지 테스트", "<p>내용</p>", boardId9, adminUser);
        article2 =
            articleFixture.코인_공지_게시글("[유저팀] 공지 테스트", "<p>내용</p>", boardId9, adminUser);
        article3 =
            articleFixture.코인_공지_게시글("[비즈니스팀] 공지 테스트", "<p>내용</p>", boardId9, adminUser);
        article4 =
            articleFixture.코인_공지_게시글("[KOIN] 공지 테스트", "<p>내용</p>", boardId9, adminUser);
    }

    @Test
    void 관리자_권한으로_코인_공지사항_게시글을_작성한다() throws Exception{
        String jsonBody = """
        {
            "title": "[코인 캠퍼스팀] 공지사항 테스트",
            "content": "<html><head><title>일반공지사항</title></head></html>"
        }
        """;

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                post("/admin/notice")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 관리자_권한으로_삭제_되지_않은_코인_공지사항_목록을_조회한다() throws Exception {
        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/notices")
                    .header("Authorization", "Bearer " + token)
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
        // 코인 공지사항 게시글 생성
        KoinArticle koinArticle = KoinArticle.builder()
            .user(adminUser)
            .isDeleted(false)
            .build();

        Article adminNoticeArticle = Article.builder()
            .board(boardId9)
            .title("[코인 캠퍼스팀] 공지사항 테스트")
            .content("<p>내용</p>")
            .koinArticle(koinArticle)
            .koreatechArticle(null)
            .isNotice(true)
            .isDeleted(false)
            .build();

        koinArticle.setArticle(adminNoticeArticle);

        Article saveNotice = articleRepository.save(adminNoticeArticle);
        Integer noticeId = saveNotice.getId();

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                get("/admin/notice/{id}", noticeId)
                    .header("Authorization", "Bearer " + token)
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
        // 코인 공지사항 게시글 생성
        KoinArticle koinArticle = KoinArticle.builder()
            .user(adminUser)
            .isDeleted(false)
            .build();

        Article adminNoticeArticle = Article.builder()
            .board(boardId9)
            .title("[코인 캠퍼스팀] 공지사항 테스트")
            .content("<p>내용</p>")
            .koinArticle(koinArticle)
            .koreatechArticle(null)
            .isNotice(true)
            .isDeleted(false)
            .build();

        koinArticle.setArticle(adminNoticeArticle);

        Article saveNotice = articleRepository.save(adminNoticeArticle);
        Integer noticeId = saveNotice.getId();

        String token = userFixture.getToken(adminUser);

        mockMvc.perform(
                delete("/admin/notice/{id}", noticeId)
                    .header("Authorization", "Bearer " + token)
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
        // 코인 공지사항 게시글 생성
        KoinArticle koinArticle = KoinArticle.builder()
            .user(adminUser)
            .isDeleted(false)
            .build();

        Article adminNoticeArticle = Article.builder()
            .board(boardId9)
            .title("[코인 캠퍼스팀] 공지사항 테스트")
            .content("<p>내용</p>")
            .koinArticle(koinArticle)
            .koreatechArticle(null)
            .isNotice(true)
            .isDeleted(false)
            .build();

        koinArticle.setArticle(adminNoticeArticle);

        Article saveNotice = articleRepository.save(adminNoticeArticle);
        Integer noticeId = saveNotice.getId();

        String token = userFixture.getToken(adminUser);

        // 코인 공지사항 게시글 수정
        String jsonBody = """
        {
            "title": "[코인 캠퍼스팀] 공지사항 테스트 수정",
            "content": "<html><head><title>일반공지사항 수정</title></head></html>"
        }
        """;

        mockMvc.perform(
                put("/admin/notice/{id}", noticeId)
                    .header("Authorization", "Bearer " + token)
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

