package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.ArticleAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.BoardAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.community.article.model.Article;
import in.koreatech.koin.domain.community.article.model.Board;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;

class ArticleApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private ArticleAcceptanceFixture articleFixture;

    @Autowired
    private BoardAcceptanceFixture boardFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    Student student;
    Department department;
    Board board, adminBoard;
    Article article1, article2, article3;

    @BeforeAll
    void givenBeforeEach() {
        clear();
        department = departmentFixture.컴퓨터공학부();
        student = userFixture.준호_학생(department, null);
        board = boardFixture.자유게시판();
        adminBoard = boardFixture.공지사항();
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
        Board noticeBoard = article3.getBoard();
        mockMvc.perform(
                get("/articles/{articleId}", article3.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("boardId", noticeBoard.getId().toString())
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

    @Test
    void 게시판_목록은_id가_아니라_등록일_기준으로_정렬된다() throws Exception {
        Board jobBoard = boardFixture.취업공지();

        // 먼저 등록된(id가 낮은) 글이지만 등록일은 더 최근이다.
        Article recent = articleFixture.공지_크롤링_게시글("최근 채용 공고", jobBoard, 90001, LocalDate.of(2026, 9, 1));
        // 나중에 등록된(id가 더 높은) 글이지만, 뒤늦게 백필된 2016년 글이라 등록일은 더 과거다.
        Article backfilledOld = articleFixture.공지_크롤링_게시글("2016년 채용 공고", jobBoard, 90002, LocalDate.of(2016, 4, 27));

        mockMvc.perform(
                get("/articles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("boardId", jobBoard.getId().toString())
                    .param("page", "1")
                    .param("limit", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.articles[0].id").value(recent.getId()))
            .andExpect(jsonPath("$.articles[0].registered_at").value("2026-09-01"))
            .andExpect(jsonPath("$.articles[1].id").value(backfilledOld.getId()))
            .andExpect(jsonPath("$.articles[1].registered_at").value("2016-04-27"));
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
