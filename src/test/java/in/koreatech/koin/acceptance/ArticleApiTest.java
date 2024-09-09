package in.koreatech.koin.acceptance;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.community.article.model.Comment;
import in.koreatech.koin.domain.community.article.repository.ArticleRepository;
import in.koreatech.koin.domain.community.article.repository.CommentRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.fixture.ArticleFixture;
import in.koreatech.koin.fixture.BoardFixture;
import in.koreatech.koin.fixture.UserFixture;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleApiTest extends AcceptanceTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ArticleFixture articleFixture;

    @Autowired
    private BoardFixture boardFixture;

    Student student;
    Board board;
    Article article1, article2;

    @BeforeAll
    void givenBeforeEach() {
        clear();
        student = userFixture.준호_학생();
        board = boardFixture.자유게시판();
        article1 = articleFixture.자유글_1(board);
        article2 = articleFixture.자유글_2(board);
    }

    @Test
    void 특정_게시글을_단일_조회한다() throws Exception {
        // given
        Comment request = Comment.builder()
            .article(article1)
            .content("댓글")
            .userId(1)
            .nickname("BCSD")
            .isDeleted(false)
            .build();
        commentRepository.save(request);

        mockMvc.perform(
                get("/articles/{articleId}", article1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "id": 1,
                     "board_id": 1,
                     "title": "자유 글의 제목입니다",
                     "content": "<p>내용</p>",
                     "author": "작성자1",
                     "hit": 3,
                     "attachments": [
                         {
                             "id": 1,
                             "name": "첨부파일1.png",
                             "url": "https://example.com",
                             "created_at": "2024-01-15 12:00:00",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ],
                     "registered_at": "2024-01-15",
                     "prev_id": null,
                     "next_id": 2,
                     "updated_at": "2024-01-15 12:00:00"
                 }
                """));
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다() throws Exception {
        // when then
        mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(1))
                    .param("limit", String.valueOf(10))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                     "articles": [
                         {
                             "id": 2,
                             "board_id": 1,
                             "title": "자유 글2의 제목입니다",
                             "author": "작성자2",
                             "hit": 2,
                             "registered_at": "2024-01-15",
                             "updated_at": "2024-01-15 12:00:00"
                         },
                         {
                             "id": 1,
                             "board_id": 1,
                             "title": "자유 글의 제목입니다",
                             "author": "작성자1",
                             "hit": 2,
                             "registered_at": "2024-01-15",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ],
                     "total_count": 2,
                     "current_count": 2,
                     "total_page": 1,
                     "current_page": 1
                 }
                """));
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_페이지가_0이면_1_페이지_조회() throws Exception {
        // when then
        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(1))
                    .param("limit", String.valueOf(0L))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles", hasSize(1)))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_페이지가_음수이면_1_페이지_조회() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(-10L))
                    .param("limit", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles[0].id", is(article2.getId())))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_limit가_0_이면_한_번에_1_게시글_조회() throws Exception {
        // when then
        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(1))
                    .param("limit", String.valueOf(0L))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles", hasSize(1)))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_limit가_음수이면_한_번에_1_게시글_조회() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(1))
                    .param("limit", String.valueOf(-10L))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles", hasSize(1)))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_limit가_50_이상이면_한_번에_50_게시글_조회() throws Exception {
        // given
        for (int i = 3; i < 63; i++) { // unique 중복 처리
            Article article = Article.builder()
                .board(board)
                .title("제목")
                .content("<p>내용</p>")
                .author("BCSD")
                .hit(14)
                .koinHit(0)
                .isDeleted(false)
                .articleNum(i)
                .url("https://example.com")
                .registeredAt(LocalDate.of(2024, 1, 15))
                .build();
            articleRepository.save(article);
        }

        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(1))
                    .param("limit", String.valueOf(100L))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles", hasSize(50)))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_페이지_limit가_주어지지_않으면_1_페이지_10_게시글_조회() throws Exception {
        // given
        for (int i = 3; i < 13; i++) { // unique 중복 처리
            Article article = Article.builder()
                .board(board)
                .title("제목")
                .content("<p>내용</p>")
                .author("BCSD")
                .hit(14)
                .koinHit(0)
                .isDeleted(false)
                .articleNum(i)
                .url("https://example.com")
                .registeredAt(LocalDate.of(2024, 1, 15))
                .build();
            articleRepository.save(article);
        }

        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles", hasSize(10)))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_특정_페이지_조회() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(2))
                    .param("limit", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                 {
                     "articles": [
                         {
                             "id": 1,
                             "board_id": 1,
                             "title": "자유 글의 제목입니다",
                             "author": "작성자1",
                             "hit": 2,
                             "registered_at": "2024-01-15",
                             "updated_at": "2024-01-15 12:00:00"
                         }
                     ],
                    "total_count": 2,
                    "current_count": 1,
                    "total_page": 2,
                    "current_page": 2
                 }
                """))
            .andReturn();
    }

    @Test
    void 게시글들을_페이지네이션하여_조회한다_최대_페이지를_초과한_요청이_들어오면_마지막_페이지를_반환한다() throws Exception {
        // when then
        MvcResult result = mockMvc.perform(
                get("/articles")
                    .param("boardId", String.valueOf(board.getId()))
                    .param("page", String.valueOf(10000L))
                    .param("limit", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                 {
                    "articles": [
                        {
                            "id": 1,
                            "board_id": 1,
                            "title": "자유 글의 제목입니다",
                            "author": "작성자1",
                            "hit": 2,
                            "registered_at": "2024-01-15",
                            "updated_at": "2024-01-15 12:00:00"
                        }
                    ],
                    "total_count": 2,
                    "current_count": 1,
                    "total_page": 2,
                    "current_page": 2
                }
                """))
            .andReturn();
    }

    @Test
    void 인기많은_게시글_목록을_조회한다() throws Exception {
        // given
        for (int i = 5; i <= 7; i++) {
            articleRepository.save(Article.builder()
                .board(board)
                .title(String.format("Article %d", i))
                .content("<p>내용</p>")
                .author("BCSD")
                .hit(i)
                .koinHit(0)
                .isDeleted(false)
                .articleNum(i)
                .url("https://example.com")
                .registeredAt(LocalDate.of(2024, 1, 15))
                .build()
            );
        }

        mockMvc.perform(
                get("/articles/hot")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                [
                      {
                          "id": 5,
                          "board_id": 1,
                          "title": "Article 7",
                          "author": "BCSD",
                          "hit": 7,
                          "registered_at": "2024-01-15",
                          "updated_at": "2024-01-15 12:00:00"
                      },
                      {
                          "id": 4,
                          "board_id": 1,
                          "title": "Article 6",
                          "author": "BCSD",
                          "hit": 6,
                          "registered_at": "2024-01-15",
                          "updated_at": "2024-01-15 12:00:00"
                      },
                      {
                          "id": 3,
                          "board_id": 1,
                          "title": "Article 5",
                          "author": "BCSD",
                          "hit": 5,
                          "registered_at": "2024-01-15",
                          "updated_at": "2024-01-15 12:00:00"
                      },
                      {
                          "id": 2,
                          "board_id": 1,
                          "title": "자유 글2의 제목입니다",
                          "author": "작성자2",
                          "hit": 2,
                          "registered_at": "2024-01-15",
                          "updated_at": "2024-01-15 12:00:00"
                      },
                      {
                          "id": 1,
                          "board_id": 1,
                          "title": "자유 글의 제목입니다",
                          "author": "작성자1",
                          "hit": 2,
                          "registered_at": "2024-01-15",
                          "updated_at": "2024-01-15 12:00:00"
                      }
                  ]
                """))
            .andReturn();
    }

    @Test
    void 게시글을_검색한다() throws Exception {
        mockMvc.perform(
                get("/articles/search")
                    .queryParam("query", "자유")
                    .queryParam("board", String.valueOf(1))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "articles": [
                        {
                            "id": 2,
                            "board_id": 1,
                            "title": "자유 글2의 제목입니다",
                            "author": "작성자2",
                            "hit": 2,
                            "registered_at": "2024-01-15",
                            "updated_at": "2024-01-15 12:00:00"
                        },
                        {
                            "id": 1,
                            "board_id": 1,
                            "title": "자유 글의 제목입니다",
                            "author": "작성자1",
                            "hit": 2,
                            "registered_at": "2024-01-15",
                            "updated_at": "2024-01-15 12:00:00"
                        }
                    ],
                    "total_count": 2,
                    "current_count": 2,
                    "total_page": 1,
                    "current_page": 1
                }
                """));
    }

    @Test
    void 사용자들이_많이_검색_한_키워드_추천() throws Exception {
        for (int i = 4; i <= 14; i++) {
            Article article = Article.builder()
                .board(board)
                .title("제목%s".formatted(i))
                .content("<p>내용333</p>")
                .author("작성자3")
                .hit(1)
                .koinHit(1)
                .isDeleted(false)
                .articleNum(i)
                .url("https://example3.com")
                .attachments(List.of())
                .registeredAt(LocalDate.of(2024, 1, 15))
                .isNotice(false)
                .build();

            articleRepository.save(article);
        }

        String ipAddress1 = "192.168.1.1";
        String ipAddress2 = "192.168.1.2";
        String ipAddress3 = "192.168.1.3";

        for (int i = 4; i < 9; i++) {
            mockMvc.perform(
                    get("/articles/search")
                        .queryParam("query", "검색어" + i)
                        .queryParam("board", String.valueOf(1))
                        .queryParam("page", String.valueOf(1))
                        .queryParam("limit", String.valueOf(10))
                        .queryParam("ipAddress", ipAddress1)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

            mockMvc.perform(
                    get("/articles/search")
                        .queryParam("query", "검색어" + i)
                        .queryParam("board", String.valueOf(1))
                        .queryParam("page", String.valueOf(1))
                        .queryParam("limit", String.valueOf(10))
                        .queryParam("ipAddress", ipAddress2)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        }

        for (int i = 9; i < 14; i++) {
            mockMvc.perform(
                    get("/articles/search")
                        .queryParam("query", "검색어" + i)
                        .queryParam("board", String.valueOf(1))
                        .queryParam("page", String.valueOf(1))
                        .queryParam("limit", String.valueOf(10))
                        .queryParam("ipAddress", ipAddress3)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        }

        mockMvc.perform(
                get("/articles/hot/keyword")
                    .queryParam("count", String.valueOf(5))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                   "keywords": [
                     "검색어4",
                     "검색어5",
                     "검색어6",
                     "검색어7",
                     "검색어8"
                   ]
                }
                """));
    }
}
