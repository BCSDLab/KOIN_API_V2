package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.community.model.Article;
import in.koreatech.koin.domain.community.model.Board;
import in.koreatech.koin.domain.community.model.Comment;
import in.koreatech.koin.domain.community.repository.ArticleRepository;
import in.koreatech.koin.domain.community.repository.BoardRepository;
import in.koreatech.koin.domain.community.repository.CommentRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class CommunityApiTest extends AcceptanceTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private JwtProvider jwtProvider;

    private final long PAGE_NUMBER = 1L;
    private final long PAGE_LIMIT = 1L;
    private final long ARTICLE_COUNT = 2L;

    private Board board;
    private Article article1, article2;
    private Student student;

    @BeforeEach
    void givenBeforeEach() {
        Student studentRequest = Student.builder()
            .studentNumber("202020136070")
            .anonymousNickname("익명")
            .department(StudentDepartment.COMPUTER)
            .userIdentity(UserIdentity.UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .password("0000")
                    .nickname("BCSD")
                    .name("송선권")
                    .phoneNumber("010-1234-5678")
                    .userType(STUDENT)
                    .gender(UserGender.MAN)
                    .email("test@koreatech.ac.kr")
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
        student = studentRepository.save(studentRequest);

        Board boardRequest = Board.builder()
            .tag("FA001")
            .name("자유게시판")
            .isAnonymous(false)
            .articleCount(338L)
            .isDeleted(false)
            .isNotice(false)
            .parentId(null)
            .seq(1L)
            .build();
        board = boardRepository.save(boardRequest);

        Article article1Request = Article.builder()
            .board(board)
            .title("Article 1")
            .content("<p>내용</p>")
            .user(student.getUser())
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
        article1 = articleRepository.save(article1Request);

        Article article2Request = Article.builder()
            .board(board)
            .title("Article 2")
            .content("<p> CONTENT</p>")
            .user(student.getUser())
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

        article2 = articleRepository.save(article2Request);
    }

    @Test
    @DisplayName("특정 게시글을 단일 조회한다.")
    void getArticle() {
        // given
        Comment request = Comment.builder()
            .article(article1)
            .content("댓글")
            .userId(1L)
            .nickname("BCSD")
            .isDeleted(false)
            .build();

        Comment comment = commentRepository.save(request);

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/articles/{articleId}", article1.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        article1.updateContentSummary();
        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("id")).isEqualTo(article1.getId());
                softly.assertThat(response.jsonPath().getLong("board_id")).isEqualTo(article1.getBoard().getId());
                softly.assertThat(response.jsonPath().getString("title")).isEqualTo(article1.getTitle());
                softly.assertThat(response.jsonPath().getString("content")).isEqualTo(article1.getContent());
                softly.assertThat(response.jsonPath().getString("nickname")).isEqualTo(article1.getNickname());
                softly.assertThat(response.jsonPath().getLong("hit")).isEqualTo(article1.getHit());
                softly.assertThat(response.jsonPath().getBoolean("is_solved")).isEqualTo(article1.getIsSolved());
                softly.assertThat(response.jsonPath().getByte("comment_count")).isEqualTo(article1.getCommentCount());
                softly.assertThat(response.jsonPath().getBoolean("is_notice")).isEqualTo(article1.getIsNotice());
                softly.assertThat(response.jsonPath().getString("contentSummary"))
                    .isEqualTo(article1.getContentSummary());

                softly.assertThat(response.jsonPath().getLong("board.id")).isEqualTo(board.getId());
                softly.assertThat(response.jsonPath().getString("board.tag")).isEqualTo(board.getTag());
                softly.assertThat(response.jsonPath().getString("board.name")).isEqualTo(board.getName());
                softly.assertThat(response.jsonPath().getBoolean("board.is_anonymous"))
                    .isEqualTo(board.getIsAnonymous());
                softly.assertThat(response.jsonPath().getLong("board.article_count"))
                    .isEqualTo(board.getArticleCount());
                softly.assertThat(response.jsonPath().getBoolean("board.is_deleted")).isEqualTo(board.getIsDeleted());
                softly.assertThat(response.jsonPath().getBoolean("board.is_notice")).isEqualTo(board.getIsNotice());
                softly.assertThat(response.jsonPath().getString("board.parent_id")).isEqualTo(board.getParentId());
                softly.assertThat(response.jsonPath().getLong("board.seq")).isEqualTo(board.getSeq());
                softly.assertThat(response.jsonPath().getString("board.children"))
                    .isEqualTo(board.getChildren().isEmpty() ? null : board.getChildren());

                softly.assertThat(response.jsonPath().getLong("comments[0].id")).isEqualTo(comment.getId());
                softly.assertThat(response.jsonPath().getLong("comments[0].article_id"))
                    .isEqualTo(comment.getArticle().getId());
                softly.assertThat(response.jsonPath().getString("comments[0].content")).isEqualTo(comment.getContent());
                softly.assertThat(response.jsonPath().getLong("comments[0].user_id")).isEqualTo(comment.getUserId());
                softly.assertThat(response.jsonPath().getString("comments[0].nickname"))
                    .isEqualTo(comment.getNickname());
                softly.assertThat(response.jsonPath().getBoolean("comments[0].is_deleted"))
                    .isEqualTo(comment.getIsDeleted());
                softly.assertThat(response.jsonPath().getBoolean("comments[0].grantEdit"))
                    .isEqualTo(comment.getGrantEdit());
                softly.assertThat(response.jsonPath().getBoolean("comments[0].grantDelete"))
                    .isEqualTo(comment.getGrantDelete());
            }
        );
    }

    @Test
    @DisplayName("특정 게시글을 단일 조회한다. - 댓글 작성자가 본인이면 수정 및 제거 권한이 부여된다.")
    void getArticleAuthorizationComment() {
        // given
        String token = jwtProvider.createToken(student.getUser());

        Comment request = Comment.builder()
            .article(article1)
            .content("댓글")
            .userId(1L)
            .nickname("BCSD")
            .isDeleted(false)
            .build();

        Comment comment = commentRepository.save(request);
        comment.updateAuthority(student.getUser().getId());

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/articles/{articleId}", article1.getId())
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        article1.updateContentSummary();
        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getBoolean("comments[0].grantEdit"))
                    .isEqualTo(comment.getGrantEdit());
                softly.assertThat(response.jsonPath().getBoolean("comments[0].grantDelete"))
                    .isEqualTo(comment.getGrantDelete());
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다.")
    void getArticlesByPagination() {
        // given

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        article2.updateContentSummary();
        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("board.id")).isEqualTo(board.getId());
                softly.assertThat(response.jsonPath().getString("board.tag")).isEqualTo(board.getTag());
                softly.assertThat(response.jsonPath().getString("board.name")).isEqualTo(board.getName());
                softly.assertThat(response.jsonPath().getBoolean("board.is_anonymous"))
                    .isEqualTo(board.getIsAnonymous());
                softly.assertThat(response.jsonPath().getLong("board.article_count"))
                    .isEqualTo(board.getArticleCount());
                softly.assertThat(response.jsonPath().getBoolean("board.is_deleted")).isEqualTo(board.getIsDeleted());
                softly.assertThat(response.jsonPath().getBoolean("board.is_notice")).isEqualTo(board.getIsNotice());
                softly.assertThat(response.jsonPath().getString("board.parent_id")).isEqualTo(board.getParentId());
                softly.assertThat(response.jsonPath().getLong("board.seq")).isEqualTo(board.getSeq());
                softly.assertThat(response.jsonPath().getString("board.children"))
                    .isEqualTo(board.getChildren().isEmpty() ? null : board.getChildren());

                softly.assertThat(response.jsonPath().getLong("articles[0].id")).isEqualTo(article2.getId());
                softly.assertThat(response.jsonPath().getLong("articles[0].board_id"))
                    .isEqualTo(article2.getBoard().getId());
                softly.assertThat(response.jsonPath().getString("articles[0].title")).isEqualTo(article2.getTitle());
                softly.assertThat(response.jsonPath().getString("articles[0].content"))
                    .isEqualTo(article2.getContent());
                softly.assertThat(response.jsonPath().getLong("articles[0].user_id"))
                    .isEqualTo(article2.getUser().getId());
                softly.assertThat(response.jsonPath().getString("articles[0].nickname"))
                    .isEqualTo(article2.getNickname());
                softly.assertThat(response.jsonPath().getLong("articles[0].hit")).isEqualTo(article2.getHit());
                softly.assertThat(response.jsonPath().getString("articles[0].ip")).isEqualTo(article2.getIp());
                softly.assertThat(response.jsonPath().getBoolean("articles[0].is_solved"))
                    .isEqualTo(article2.getIsSolved());
                softly.assertThat(response.jsonPath().getBoolean("articles[0].is_deleted"))
                    .isEqualTo(article2.getIsDeleted());
                softly.assertThat(response.jsonPath().getByte("articles[0].comment_count"))
                    .isEqualTo(article2.getCommentCount());
                softly.assertThat(response.jsonPath().getString("articles[0].meta")).isEqualTo(article2.getMeta());
                softly.assertThat(response.jsonPath().getBoolean("articles[0].is_notice"))
                    .isEqualTo(article2.getIsNotice());
                softly.assertThat(response.jsonPath().getString("articles[0].notice_article_id"))
                    .isEqualTo(article2.getNoticeArticleId());
                softly.assertThat(response.jsonPath().getString("articles[0].summary"))
                    .isEqualTo(article2.getSummary());
                softly.assertThat(response.jsonPath().getString("articles[0].contentSummary"))
                    .isEqualTo(article2.getContentSummary());

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
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER_ZERO)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
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
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER_MINUS)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
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
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT_ZERO)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
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
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT_ZERO)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
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
                .board(board)
                .title("제목")
                .content("<p>내용</p>")
                .user(student.getUser())
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
        }

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT_ZERO)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("totalPage"))
                    .isEqualTo((long)Math.ceil(((double)ARTICLE_COUNT + ADD_ARTICLE_COUNT) / MAX_PAGE_LIMIT));
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
                .board(board)
                .title("제목")
                .content("<p>내용</p>")
                .user(student.getUser())
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
        }

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getLong("articles[0].id")).isEqualTo(FINAL_ARTICLE_ID);
                softly.assertThat(response.jsonPath().getLong("totalPage"))
                    .isEqualTo((long)Math.ceil(((double)ARTICLE_COUNT + ADD_ARTICLE_COUNT) / DEFAULT_LIMIT));
            }
        );
    }

    @Test
    @DisplayName("게시글들을 페이지네이션하여 조회한다. - 요청된 페이지에 게시글이 존재하지 않으면 빈 게시글 배열을 반환한다.")
    void getArticlesByPagination_overMaxPageNotFound() {
        // given
        final long PAGE_NUMBER = 10000L;

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("boardId", board.getId())
            .param("page", PAGE_NUMBER)
            .param("limit", PAGE_LIMIT)
            .get("/articles")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getList("articles")).hasSize(0);
            }
        );
    }

    @Test
    @DisplayName("인기많은 게시글 목록을 조회한다.")
    void getHotArticles() {
        // given
        final int ARTICLE_COUNT = 30;
        final int HOT_ARTICLE_LIMIT = 10;
        List<Article> articles = new ArrayList<>();

        for (int i = 1; i <= ARTICLE_COUNT; i++) {
            articles.add(
                Article.builder()
                    .board(board)
                    .title(String.format("Article %d", i))
                    .content("<p>내용</p>")
                    .user(student.getUser())
                    .nickname("BCSD")
                    .hit((long)i)
                    .ip("123.21.234.321")
                    .isSolved(false)
                    .isDeleted(false)
                    .commentCount((byte)2)
                    .meta(null)
                    .isNotice(false)
                    .noticeArticleId(null)
                    .build()
            );
            articleRepository.save(articles.get(i - 1));
        }

        // when then
        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .get("/articles/hot/list")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.jsonPath().getList("").size()).isEqualTo(HOT_ARTICLE_LIMIT);

                softly.assertThat(response.jsonPath().getLong("[0].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 1).getId());
                softly.assertThat(response.jsonPath().getLong("[0].board_id")).isEqualTo(board.getId());
                softly.assertThat(response.jsonPath().getString("[0].title"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 1).getTitle());
                softly.assertThat(response.jsonPath().getString("[0].contentSummary"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 1).getContentSummary());
                softly.assertThat(response.jsonPath().getByte("[0].comment_count"))
                    .isEqualTo((byte)articles.get(ARTICLE_COUNT - 1).getCommentCount());
                softly.assertThat(response.jsonPath().getLong("[0].hit"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 1).getHit());

                softly.assertThat(response.jsonPath().getLong("[1].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 2).getId());
                softly.assertThat(response.jsonPath().getLong("[2].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 3).getId());
                softly.assertThat(response.jsonPath().getLong("[3].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 4).getId());
                softly.assertThat(response.jsonPath().getLong("[4].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 5).getId());
                softly.assertThat(response.jsonPath().getLong("[5].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 6).getId());
                softly.assertThat(response.jsonPath().getLong("[6].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 7).getId());
                softly.assertThat(response.jsonPath().getLong("[7].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 8).getId());
                softly.assertThat(response.jsonPath().getLong("[8].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 9).getId());
                softly.assertThat(response.jsonPath().getLong("[9].id"))
                    .isEqualTo(articles.get(ARTICLE_COUNT - 10).getId());
            }
        );
    }
}
