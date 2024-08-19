package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.articles.model.Article;
import in.koreatech.koin.domain.community.keywords.model.ArticleKeywordUserMap;
import in.koreatech.koin.domain.community.articles.model.Board;
import in.koreatech.koin.domain.community.articles.model.Comment;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordRepository;
import in.koreatech.koin.domain.community.keywords.repository.ArticleKeywordUserMapRepository;
import in.koreatech.koin.domain.community.articles.repository.ArticleRepository;
import in.koreatech.koin.domain.community.articles.repository.CommentRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.fixture.ArticleFixture;
import in.koreatech.koin.fixture.BoardFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class CommunityApiTest extends AcceptanceTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleKeywordRepository articleKeywordRepository;

    @Autowired
    private ArticleKeywordUserMapRepository articleKeywordUserMapRepository;

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private ArticleFixture articleFixture;

    @Autowired
    private BoardFixture boardFixture;

    Student student;
    Board board;
    Article article1, article2;

    @BeforeEach
    void givenBeforeEach() {
        student = userFixture.준호_학생();
        board = boardFixture.자유게시판();
        article1 = articleFixture.자유글_1(student.getUser(), board);
        article2 = articleFixture.자유글_2(student.getUser(), board);
    }

    @Test
    @DisplayName("특정 게시글을 단일 조회한다.")
    void getArticle() {
        // given
        Comment request = Comment.builder()
            .article(article1)
            .content("댓글")
            .userId(1)
            .nickname("BCSD")
            .isDeleted(false)
            .build();
        commentRepository.save(request);

        // when then
        var response = RestAssured
            .given()
            .when()
            .get("/articles/{articleId}", article1.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "board_id": 1,
                    "title": "자유 글의 제목입니다",
                    "content": "<p>내용</p>",
                    "nickname": "준호",
                    "hit": 1,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("특정 게시글을 단일 조회한다. - 댓글 작성자가 본인이면 수정 및 제거 권한이 부여된다.")
    void getArticleAuthorizationComment() {
        // given
        String token = userFixture.getToken(student.getUser());

        Comment request = Comment.builder()
            .article(article1)
            .content("댓글")
            .userId(1)
            .nickname("BCSD")
            .isDeleted(false)
            .build();

        Comment comment = commentRepository.save(request);
        comment.updateAuthority(student.getUser().getId());

        // when then
        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/articles/{articleId}", article1.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "id": 1,
                    "board_id": 1,
                    "title": "자유 글의 제목입니다",
                    "content": "<p>내용</p>",
                    "nickname": "준호",
                    "hit": 2,
                    "created_at": "2024-01-15 12:00:00",
                    "updated_at": "2024-01-15 12:00:00"
                }
                """);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다.")
    void getArticlesByPagination() {
        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 1)
            .param("limit", 10)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "articles": [
                        {
                            "id": 2,
                            "board_id": 1,
                            "title": "자유 글2의 제목입니다",
                            "nickname": "준호",
                            "hit": 1,
                            "created_at": "2024-01-15 12:00:00",
                            "updated_at": "2024-01-15 12:00:00"
                        },
                        {
                            "id": 1,
                            "board_id": 1,
                            "title": "자유 글의 제목입니다",
                            "nickname": "준호",
                            "hit": 1,
                            "created_at": "2024-01-15 12:00:00",
                            "updated_at": "2024-01-15 12:00:00"
                        }
                    ],
                    "total_count": 2,
                    "current_count": 2,
                    "total_page": 1,
                    "current_page": 1
                }
                """);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지가 0이면 1 페이지 조회")
    void getArticlesByPagination_0Page() {
        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 0L)
            .param("limit", 1)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getInt("articles[0].id")).isEqualTo(article2.getId());
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지가 음수이면 1 페이지 조회")
    void getArticlesByPagination_lessThan0Pages() {
        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", -10L)
            .param("limit", 1)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getInt("articles[0].id")).isEqualTo(article2.getId());
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - limit가 0 이면 한 번에 1 게시글 조회")
    void getArticlesByPagination_1imit() {
        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 1)
            .param("limit", 0L)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getList("articles")).hasSize(1);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - limit가 음수이면 한 번에 1 게시글 조회")
    void getArticlesByPagination_lessThan0Limit() {
        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 1)
            .param("limit", -10L)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getList("articles")).hasSize(1);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - limit가 50 이상이면 한 번에 50 게시글 조회")
    void getArticlesByPagination_over50Limit() {
        // given
        for (int i = 0; i < 60; i++) {
            Article article = Article.builder()
                .board(board)
                .title("제목")
                .content("<p>내용</p>")
                .user(student.getUser())
                .nickname("BCSD")
                .hit(14)
                .ip("123.21.234.321")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)2)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build();
            articleRepository.save(article);
        }

        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 1)
            .param("limit", 100L)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getList("articles")).hasSize(50);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지, limit가 주어지지 않으면 1 페이지 10 게시글 조회")
    void getArticlesByPagination_default() {
        // given
        for (int i = 0; i < 10; i++) {
            Article article = Article.builder()
                .board(board)
                .title("제목")
                .content("<p>내용</p>")
                .user(student.getUser())
                .nickname("BCSD")
                .hit(14)
                .ip("123.21.234.321")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)2)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build();
            articleRepository.save(article);
        }

        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getList("articles")).hasSize(10);

    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 특정 페이지 조회")
    void getArticlesByPagination_pageTest() {
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 2)
            .param("limit", 1)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "articles": [
                        {
                            "id": 1,
                            "board_id": 1,
                            "title": "자유 글의 제목입니다",
                            "nickname": "준호",
                            "hit": 1,
                            "created_at": "2024-01-15 12:00:00",
                            "updated_at": "2024-01-15 12:00:00"
                        }
                    ],
                   "total_count": 2,
                   "current_count": 1,
                   "total_page": 2,
                   "current_page": 2
                }
                """);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 최대 페이지를 초과한 요청이 들어오면 마지막 페이지를 반환한다.")
    void getArticlesByPagination_overMaxPageNotFound() {
        // when then
        var response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", 10000L)
            .param("limit", 1)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                   {
                       "articles": [
                           {
                               "id": 1,
                               "board_id": 1,
                               "title": "자유 글의 제목입니다",
                               "nickname": "준호",
                               "hit": 1,
                               "created_at": "2024-01-15 12:00:00",
                               "updated_at": "2024-01-15 12:00:00"
                           }
                       ],
                       "total_count": 2,
                       "current_count": 1,
                       "total_page": 2,
                       "current_page": 2
                   }
                """);
    }

    @Test
    @DisplayName("인기많은 게시글 목록을 조회한다.")
    void getHotArticles() {
        // given
        for (int i = 5; i <= 7; i++) {
            articleRepository.save(Article.builder()
                .board(board)
                .title(String.format("Article %d", i))
                .content("<p>내용</p>")
                .user(student.getUser())
                .nickname("BCSD")
                .hit(i)
                .ip("123.21.234.321")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)2)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build()
            );
        }

        // when then
        var response = RestAssured
            .given()
            .when()
            .get("/articles/hot")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                [
                    {
                        "id": 5,
                        "board_id": 1,
                        "title": "Article 7",
                        "nickname": "BCSD",
                        "hit": 7,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 4,
                        "board_id": 1,
                        "title": "Article 6",
                        "nickname": "BCSD",
                        "hit": 6,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 3,
                        "board_id": 1,
                        "title": "Article 5",
                        "nickname": "BCSD",
                        "hit": 5,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 2,
                        "board_id": 1,
                        "title": "자유 글2의 제목입니다",
                        "nickname": "준호",
                        "hit": 1,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 1,
                        "board_id": 1,
                        "title": "자유 글의 제목입니다",
                        "nickname": "준호",
                        "hit": 1,
                        "created_at": "2024-01-15 12:00:00",
                        "updated_at": "2024-01-15 12:00:00"
                    }
                ]
                """);
    }

    @Test
    @DisplayName("게시글을 검색한다.")
    void searchNoticeArticles() {
        var response = RestAssured
            .given()
            .when()
            .queryParam("query", "자유")
            .queryParam("board", 1)
            .get("/articles/search")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                   {
                       "articles": [
                           {
                               "id": 2,
                               "board_id": 1,
                               "title": "자유 글2의 제목입니다",
                               "nickname": "준호",
                               "hit": 1,
                               "created_at": "2024-01-15 12:00:00",
                               "updated_at": "2024-01-15 12:00:00"
                           },
                           {
                               "id": 1,
                               "board_id": 1,
                               "title": "자유 글의 제목입니다",
                               "nickname": "준호",
                               "hit": 1,
                               "created_at": "2024-01-15 12:00:00",
                               "updated_at": "2024-01-15 12:00:00"
                           }
                       ],
                       "total_count": 2,
                       "current_count": 2,
                       "total_page": 1,
                       "current_page": 1
                   }
                """);
    }

    @Test
    @DisplayName("알림 키워드를 추가한다.")
    void 알림_키워드_추가() {
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "keyword": "장학금"
                }
                """)
            .when()
            .post("/articles/keyword")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                  "id": 1,
                  "keyword": "장학금"
                }
                """);
    }

    @Test
    @DisplayName("알림 키워드를 추가한다. - 10개 넘어가면 400에러 반환")
    void 알림_키워드_10개_넘게_추가시_에러() {
        String token = userFixture.getToken(student.getUser());

        for (int i = 0; i < 10; i++) {
            RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                        "keyword": "keyword%d"
                        }
                    """, i))

                .when()
                .post("/articles/keyword")
                .then()
                .statusCode(HttpStatus.OK.value());
        }

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "keyword": "장학금"
                }
                """)
            .when()
            .post("/articles/keyword")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("알림 키워드를 삭제한다.")
    void 알림_키워드_삭제() {
        String token = userFixture.getToken(student.getUser());
        ArticleKeywordUserMap articleKeywordUserMap = articleFixture.키워드1("수강 신청", student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .pathParam("id", articleKeywordUserMap.getId())
            .contentType(ContentType.JSON)
            .when()
            .delete("/articles/keyword/{id}")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract()
            .asString();

        assertThat(articleKeywordUserMapRepository.findById(articleKeywordUserMap.getId()).isEmpty());
        assertThat(articleKeywordRepository.findById(articleKeywordUserMap.getArticleKeyword().getId()).isEmpty());
    }
}
