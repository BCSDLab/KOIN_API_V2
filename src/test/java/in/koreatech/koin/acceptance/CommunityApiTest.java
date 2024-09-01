package in.koreatech.koin.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;

@SuppressWarnings("NonAsciiCharacters")
class CommunityApiTest extends AcceptanceTest {

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

    @BeforeEach
    void givenBeforeEach() {
        student = userFixture.준호_학생();
        board = boardFixture.자유게시판();
        article1 = articleFixture.자유글_1(board);
        article2 = articleFixture.자유글_2(board);
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
                    "author": "작성자1",
                    "hit": 1,
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
                            "author": "작성자2",
                            "hit": 1,
                            "registered_at": "2024-01-15",
                            "updated_at": "2024-01-15 12:00:00"
                        },
                        {
                            "id": 1,
                            "board_id": 1,
                            "title": "자유 글의 제목입니다",
                            "author": "작성자1",
                            "hit": 1,
                            "registered_at": "2024-01-15",
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
                            "author": "작성자1",
                            "hit": 1,
                            "registered_at": "2024-01-15",
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
                               "author": "작성자1",
                               "hit": 1,
                               "registered_at": "2024-01-15",
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
                        "hit": 1,
                        "registered_at": "2024-01-15",
                        "updated_at": "2024-01-15 12:00:00"
                    },
                    {
                        "id": 1,
                        "board_id": 1,
                        "title": "자유 글의 제목입니다",
                        "author": "작성자1",
                        "hit": 1,
                        "registered_at": "2024-01-15",
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
                               "author": "작성자2",
                               "hit": 1,
                               "registered_at": "2024-01-15",
                               "updated_at": "2024-01-15 12:00:00"
                           },
                           {
                               "id": 1,
                               "board_id": 1,
                               "title": "자유 글의 제목입니다",
                               "author": "작성자1",
                               "hit": 1,
                               "registered_at": "2024-01-15",
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
    void 사용자들이_많이_검색_한_키워드_추천() {
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
            RestAssured
                .given()
                .queryParam("query", "검색어" + i)
                .queryParam("board", 1)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .queryParam("ipAddress", ipAddress1)
                .when()
                .get("/articles/search")
                .then()
                .statusCode(HttpStatus.OK.value());

            RestAssured
                .given()
                .queryParam("query", "검색어" + i)
                .queryParam("board", 1)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .queryParam("ipAddress", ipAddress2)
                .when()
                .get("/articles/search")
                .then()
                .statusCode(HttpStatus.OK.value());
        }

        for (int i = 9; i < 14; i++) {
            RestAssured
                .given()
                .queryParam("query", "검색어" + i)
                .queryParam("board", 1)
                .queryParam("page", 1)
                .queryParam("limit", 10)
                .queryParam("ipAddress", ipAddress3)
                .when()
                .get("/articles/search")
                .then()
                .statusCode(HttpStatus.OK.value());
        }

        var response = RestAssured
            .given()
            .queryParam("count", 5)
            .when()
            .get("/articles/hot/keyword")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .asPrettyString();

        JsonAssertions.assertThat(response).isEqualTo("""
                {
                  "keywords": [
                    "검색어8",
                    "검색어7",
                    "검색어6",
                    "검색어5",
                    "검색어4"
                  ]
                }
            """);
    }
}
