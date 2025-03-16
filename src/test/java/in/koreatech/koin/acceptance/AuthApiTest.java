package in.koreatech.koin.acceptance;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private String 맥북_userAgent_헤더;
    private String 리프레쉬_토큰_KEY;
    private User 코인_유저;

    @BeforeAll
    void setup() {
        clear();
        맥북_userAgent_헤더 = userFixture.맥북userAgent헤더();
        코인_유저 = userFixture.코인_유저();
        리프레쉬_토큰_KEY = "refreshToken:" + 코인_유저.getId() + ":PC";
    }

    @Test
    void 사용자가_로그인을_수행한다() throws Exception {
        mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email": "test@koreatech.ac.kr",
                          "password": "1234"
                        }
                        """
                    )
                    .header("User-Agent", 맥북_userAgent_헤더)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(
                jsonPath("$.refresh_token").value(redisTemplate.opsForValue().get(리프레쉬_토큰_KEY)))
            .andExpect(jsonPath("$.user_type").value(코인_유저.getUserType().name()))
            .andReturn();
    }

    @Test
    void 사용자가_로그인_이후_로그아웃을_수행한다() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email": "test@koreatech.ac.kr",
                          "password": "1234"
                        }
                        """
                    )
                    .header("User-Agent", 맥북_userAgent_헤더)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode jsonNode = JsonAssertions.convertJsonNode(result);
        mockMvc.perform(
                post("/user/logout")
                    .header("Authorization", "Bearer " + jsonNode.get("token").asText())
                    .header("User-Agent", 맥북_userAgent_헤더)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        Assertions.assertThat(redisTemplate.opsForValue().get(리프레쉬_토큰_KEY)).isNull();
    }

    @Test
    void 사용자가_로그인_이후_refreshToken을_재발급한다() throws Exception {
        MvcResult loginResult = mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email": "test@koreatech.ac.kr",
                          "password": "1234"
                        }
                        """
                    )
                    .header("User-Agent", 맥북_userAgent_헤더)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode loginJsonNode = JsonAssertions.convertJsonNode(loginResult);

        MvcResult refreshResult = mockMvc.perform(
                post("/user/refresh")
                    .content(String.format("""
                        {
                            "refresh_token": "%s"
                        }
                        """, loginJsonNode.get("refresh_token").asText())
                    )
                    .header("User-Agent", 맥북_userAgent_헤더)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode refreshJsonNode = JsonAssertions.convertJsonNode(refreshResult);

        String refreshToken = redisTemplate.opsForValue().get(리프레쉬_토큰_KEY);

        JsonAssertions.assertThat(refreshResult.getResponse().getContentAsString())
            .isEqualTo(String.format("""
                    {
                        "token": "%s",
                        "refresh_token": "%s"
                    }
                    """,
                refreshJsonNode.get("token").asText(),
                refreshToken
            ));
    }
}
