package in.koreatech.koin.acceptance;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class CommunityApiTest extends AcceptanceTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private BoardRepository boardRepository;

    private final long PAGE_NUMBER = 1L;
    private final long PAGE_LIMIT = 1L;
    private final long ARTICLE_COUNT = 2L;

    private Board board;
    private Article article1, article2;

    @BeforeEach
    void givenBeforeEach() {
        board = Board.builder()
            .tag("FA001")
            .name("자유게시판")
            .isAnonymous(false)
            .articleCount(338L)
            .isDeleted(false)
            .isNotice(false)
            .parentId(null)
            .seq(1L)
            .build();

        article1 = Article.builder()
            .boardId(1L)
            .title("제목")
            .content("<p>내용</p>")
            .userId(1L)
            .nickname("BCSD")
            .hit(14L)
            .ip("123.21.234.321")
            .isSolved(false)
            .isDeleted(false)
            .commentCount((byte)2)
            .meta(null)
            .isNotice(false)
            .noticeArticleId(null)
            .build();

        article2 = Article.builder()
            .boardId(1L)
            .title("TITLE")
            .content("<p> CONTENT</p>")
            .userId(1L)
            .nickname("BCSD")
            .hit(14L)
            .ip("123.14.321.213")
            .isSolved(false)
            .isDeleted(false)
            .commentCount((byte)2)
            .meta(null)
            .isNotice(false)
            .noticeArticleId(null)
            .build();

        boardRepository.save(board);
        articleRepository.save(article1);
        articleRepository.save(article2);
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다.")
    void getArticlesByPagination() {
        // given

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("board.id")).isEqualTo(board.getId());
                softly.assertThat(response.jsonPath().getString("board.tag")).isEqualTo(board.getTag());
                softly.assertThat(response.jsonPath().getString("board.name")).isEqualTo(board.getName());
                softly.assertThat(response.jsonPath().getBoolean("board.is_anonymous")).isEqualTo(board.getIsAnonymous());
                softly.assertThat(response.jsonPath().getLong("board.article_count")).isEqualTo(board.getArticleCount());
                softly.assertThat(response.jsonPath().getBoolean("board.is_deleted")).isEqualTo(board.getIsDeleted());
                softly.assertThat(response.jsonPath().getBoolean("board.is_notice")).isEqualTo(board.getIsNotice());
                softly.assertThat(response.jsonPath().getString("board.parent_id")).isEqualTo(board.getParentId());
                softly.assertThat(response.jsonPath().getLong("board.seq")).isEqualTo(board.getSeq());
                softly.assertThat(response.jsonPath().getString("board.children")).isEqualTo(board.getChildren().isEmpty() ? null : board.getChildren());

                softly.assertThat(response.jsonPath().getLong("articles[0].id")).isEqualTo(article2.getId());
                softly.assertThat(response.jsonPath().getLong("articles[0].board_id")).isEqualTo(article2.getBoardId());
                softly.assertThat(response.jsonPath().getString("articles[0].title")).isEqualTo(article2.getTitle());
                softly.assertThat(response.jsonPath().getString("articles[0].content")).isEqualTo(article2.getContent());
                softly.assertThat(response.jsonPath().getLong("articles[0].user_id")).isEqualTo(article2.getUserId());
                softly.assertThat(response.jsonPath().getString("articles[0].nickname")).isEqualTo(article2.getNickname());
                softly.assertThat(response.jsonPath().getLong("articles[0].hit")).isEqualTo(article2.getHit());
                softly.assertThat(response.jsonPath().getString("articles[0].ip")).isEqualTo(article2.getIp());
                softly.assertThat(response.jsonPath().getBoolean("articles[0].is_solved")).isEqualTo(article2.getIsSolved());
                softly.assertThat(response.jsonPath().getBoolean("articles[0].is_deleted")).isEqualTo(article2.getIsDeleted());
                softly.assertThat(response.jsonPath().getByte("articles[0].comment_count")).isEqualTo(article2.getCommentCount());
                softly.assertThat(response.jsonPath().getString("articles[0].meta")).isEqualTo(article2.getMeta());
                softly.assertThat(response.jsonPath().getBoolean("articles[0].is_notice")).isEqualTo(article2.getIsNotice());
                softly.assertThat(response.jsonPath().getString("articles[0].notice_article_id")).isEqualTo(article2.getNoticeArticleId());
                softly.assertThat(response.jsonPath().getString("articles[0].summary")).isEqualTo(article2.getSummary());
                softly.assertThat(response.jsonPath().getString("articles[0].contentSummary")).isEqualTo(article2.getContentSummary());

                softly.assertThat(response.jsonPath().getLong("totalPage")).isEqualTo(ARTICLE_COUNT / PAGE_LIMIT);
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지가 0이면 1 페이지 조회")
    void getArticlesByPagination_0Page() {
        // given
        final long PAGE_NUMBER_ZERO = 0L;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER_ZERO)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("articles[0].id")).isEqualTo(article2.getId());
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지가 음수이면 1 페이지 조회")
    void getArticlesByPagination_lessThan0Pages() {
        // given
        final long PAGE_NUMBER_MINUS = -10L;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER_MINUS)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("articles[0].id")).isEqualTo(article2.getId());
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - limit가 0 이면 한 번에 1 게시글 조회")
    void getArticlesByPagination_1Limit() {
        // given
        final long PAGE_LIMIT_ZERO = 0L;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT_ZERO)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("totalPage")).isEqualTo(ARTICLE_COUNT / PAGE_LIMIT);
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - limit가 음수이면 한 번에 1 게시글 조회")
    void getArticlesByPagination_lessThan0Limit() {
        // given
        final long PAGE_LIMIT_ZERO = -10L;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT_ZERO)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("totalPage")).isEqualTo(ARTICLE_COUNT / PAGE_LIMIT);
            }
        );
    }
    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - limit가 50 이상이면 한 번에 50 게시글 조회")
    void getArticlesByPagination_over50Limit() {
        // given
        final long PAGE_LIMIT_ZERO = 100L;
        final long MAX_PAGE_LIMIT = 50L;
        final long ADD_ARTICLE_COUNT = 60L;

        for (int i = 0; i < ADD_ARTICLE_COUNT; i++) {
            Article article = Article.builder()
                .boardId(1L)
                .title("제목")
                .content("<p>내용</p>")
                .userId(1L)
                .nickname("BCSD")
                .hit(14L)
                .ip("123.21.234.321")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)2)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build();
            articleRepository.save(article);
        };

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT_ZERO)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("totalPage")).isEqualTo((long)Math.ceil(((double)ARTICLE_COUNT + ADD_ARTICLE_COUNT) / MAX_PAGE_LIMIT));
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지, limit가 주어지지 않으면 1 페이지 10 게시글 조회")
    void getArticlesByPagination_default() {
        // given
        final long DEFAULT_LIMIT = 10L;
        final long ADD_ARTICLE_COUNT = 10L;
        final long FINAL_ARTICLE_ID = ARTICLE_COUNT + ADD_ARTICLE_COUNT;

        for (int i = 0; i < ADD_ARTICLE_COUNT; i++) {
            Article article = Article.builder()
                .boardId(1L)
                .title("제목")
                .content("<p>내용</p>")
                .userId(1L)
                .nickname("BCSD")
                .hit(14L)
                .ip("123.21.234.321")
                .isSolved(false)
                .isDeleted(false)
                .commentCount((byte)2)
                .meta(null)
                .isNotice(false)
                .noticeArticleId(null)
                .build();
            articleRepository.save(article);
        };

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        SoftAssertions.assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("articles[0].id")).isEqualTo(FINAL_ARTICLE_ID);
                softly.assertThat(response.jsonPath().getLong("totalPage")).isEqualTo((long)Math.ceil(((double)ARTICLE_COUNT + ADD_ARTICLE_COUNT) / DEFAULT_LIMIT));
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 페이지가 최대 페이지 수를 넘어가면 404")
    void getArticlesByPagination_overMaxPageNotFound() {
        // given
        final long PAGE_NUMBER = 10000L;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .when()
            .log().all()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }
}
