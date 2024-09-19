package in.koreatech.koin.acceptance;

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

    // 클래스 단에 transactional이 붙으면 테스트 실패 함
    /* @Test
    void 같은_ip_동일한_query로_4개의_스레드가_동시에_검색시_동시성_제어() throws InterruptedException {
        String query = "sameQuery";
        String ipAddress = "127.0.0.1";

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        List<Response> responseList = new ArrayList<>();

        Runnable searchTask = () -> {
            Response response = RestAssured
                .given()
                .queryParam("query", query)
                .queryParam("boardId", 1)
                .queryParam("page", 0)
                .queryParam("limit", 10)
                .header("X-Forwarded-For", ipAddress)
                .when()
                .get("articles/search");
            responseList.add(response);
            latch.countDown();
        };

        for (int i = 0; i < 4; i++) {
            executor.submit(searchTask);
        }

        latch.await();

        long successCount = responseList.stream()
            .filter(response -> response.getStatusCode() == 200)
            .count();

        assertThat(successCount).isEqualTo(4);

        executor.shutdown();
    }

    @Test
    void 다른_IP에서_동일한_쿼리로_동시에_검색시_동시성_처리() throws InterruptedException {
        String query = "sameQuery";

        List<String> ipAddresses = List.of("127.0.0.1", "192.168.0.1", "10.0.0.1", "172.16.0.1");

        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);

        List<Response> responseList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String ipAddress = ipAddresses.get(i);
            Runnable searchTask = () -> {
                Response response = RestAssured
                    .given()
                    .queryParam("query", query)
                    .queryParam("boardId", 1)
                    .queryParam("page", 0)
                    .queryParam("limit", 10)
                    .header("X-Forwarded-For", ipAddress)
                    .when()
                    .get("articles/search");
                responseList.add(response);
                latch.countDown();
            };

            executor.submit(searchTask);
        }

        latch.await();

        long successCount = responseList.stream()
            .filter(response -> response.getStatusCode() == 200)
            .count();

        assertThat(successCount).isEqualTo(4);

        executor.shutdown();
    } */
}
