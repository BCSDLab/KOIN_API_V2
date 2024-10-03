package in.koreatech.koin.acceptance;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import in.koreatech.koin.domain.community.article.model.Comment;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.CommentRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.fixture.ArticleFixture;
import in.koreatech.koin.fixture.BoardFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ArticleFixture articleFixture;

    @Autowired
    private BoardFixture boardFixture;

    Student student;
    Board board, adminBoard;
    Article article1, article2, article3;

    @BeforeAll
    void givenBeforeEach() {
        clear();
        student = userFixture.준호_학생();
        board = boardFixture.자유게시판();
        adminBoard = boardFixture.공지게시판();
        article1 = articleFixture.자유글_1(board, student.getUser());
        article2 = articleFixture.자유글_2(board, student.getUser());
        article3 = articleFixture.공지_크롤링_게시글("[취창업지원팀] 근로장학생 모집", adminBoard, 16660);
    }

    @Test
    void 특정_게시글을_단일_조회한다() throws Exception {
        // given

        mockMvc.perform(
                get("/articles/{articleId}", article1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "id": 1,
                     "board_id": 1,
                     "title": "자유글 1의 제목입니다",
                     "content": "<p>내용</p>",
                     "author": "테스트용_준호",
                     "hit": 2,
                     "attachments": [
                         {
                             "id": 1,
                             "name": "첨부파일1.png",
                             "url": "https://example.com",
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ],
                     "prev_id": null,
                     "next_id": 2,
                     "updated_at": "2024-01-15 12:00:00"
                 }
                """));
    }

    @Test
    void 공지사항_크롤링_게시글을_단건_조회한다() throws Exception {
        mockMvc.perform(
                get("/articles/{articleId}", article3.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "id": 3,
                     "board_id": 2,
                     "title": "[취창업지원팀] 근로장학생 모집",
                     "content": "<p>내용</p>",
                     "author": "취창업 지원팀",
                     "hit": 3,
                     "attachments": [
                         {
                             "id": 2,
                             "name": "첨부파일1.png",
                             "url": "https://example.com",
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ],
                     "registered_at": "2024-10-03",
                     "prev_id": null,
                     "next_id": null,
                     "updated_at": "2024-01-15 12:00:00"
                 }
                """));
    }
}
