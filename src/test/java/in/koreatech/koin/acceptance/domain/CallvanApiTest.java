package in.koreatech.koin.acceptance.domain;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CallvanAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.user.model.User;

@SuppressWarnings("NonAsciiCharacters")
class CallvanApiTest extends AcceptanceTest {

    @Autowired
    private CallvanAcceptanceFixture callvanFixture;

    @Autowired
    private UserAcceptanceFixture userFixture;

    private User author;
    private User participant;
    private User viewer;
    private CallvanPost expiredPost;
    private CallvanPost activePost;
    private CallvanPost completedExpiredPost;

    @BeforeAll
    void setUp() {
        clear();
        author = callvanFixture.콜벤_유저("callvanauthor", "작성자");
        participant = callvanFixture.콜벤_유저("callvanparticipant", "참여자");
        viewer = callvanFixture.콜벤_유저("callvanviewer", "일반유저");

        expiredPost = callvanFixture.콜벤팟(author, LocalDateTime.now().minusHours(2));
        callvanFixture.참여(expiredPost, participant);

        activePost = callvanFixture.콜벤팟(author, LocalDateTime.now().plusDays(1));

        completedExpiredPost = callvanFixture.콜벤팟(author, LocalDateTime.now().minusHours(3));
        callvanFixture.완료_처리(completedExpiredPost);
    }

    @Test
    void 일반_사용자가_목록을_조회하면_만료된_콜벤팟은_제외된다() throws Exception {
        mockMvc.perform(
                get("/callvan")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts[*].id").value(not(hasItem(expiredPost.getId()))))
            .andExpect(jsonPath("$.posts[*].id").value(hasItem(activePost.getId())));
    }

    @Test
    void 작성자_본인이_조회하면_만료된_콜벤팟도_노출된다() throws Exception {
        String token = userFixture.getToken(author);

        mockMvc.perform(
                get("/callvan")
                    .param("author", "MY")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts[*].id").value(hasItem(expiredPost.getId())));
    }

    @Test
    void 참여자_본인이_조회하면_참여한_만료된_콜벤팟도_노출된다() throws Exception {
        String token = userFixture.getToken(participant);

        mockMvc.perform(
                get("/callvan")
                    .param("joined", "true")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts[*].id").value(hasItem(expiredPost.getId())));
    }

    @Test
    void 만료된_콜벤팟에_신규_참여를_시도하면_예외가_발생한다() throws Exception {
        String token = userFixture.getToken(viewer);

        mockMvc.perform(
                post("/callvan/posts/{postId}/participants", expiredPost.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("출발 시간이 지나서 참여할 수 없습니다."));
    }

    @Test
    void 일반_사용자가_완료_상태만_조회하면_만료된_완료_콜벤팟도_노출된다() throws Exception {
        mockMvc.perform(
                get("/callvan")
                    .param("statuses", "COMPLETED")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts[*].id").value(hasItem(completedExpiredPost.getId())));
    }

    @Test
    void 일반_사용자가_요약_조회를_하면_만료된_콜벤팟은_찾을_수_없다() throws Exception {
        String token = userFixture.getToken(viewer);

        mockMvc.perform(
                get("/callvan/posts/{postId}/summary", expiredPost.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 참여자_본인은_만료된_콜벤팟도_요약_조회할_수_있다() throws Exception {
        String token = userFixture.getToken(participant);

        mockMvc.perform(
                get("/callvan/posts/{postId}/summary", expiredPost.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expiredPost.getId()));
    }

    @Test
    void 일반_사용자는_목록에_노출된_완료된_콜벤팟을_요약_조회할_수_있다() throws Exception {
        mockMvc.perform(
                get("/callvan/posts/{postId}/summary", completedExpiredPost.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(completedExpiredPost.getId()));
    }
}
