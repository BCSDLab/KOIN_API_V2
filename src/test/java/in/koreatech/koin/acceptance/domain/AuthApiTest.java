package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.RefreshTokenRedisRepository;

class AuthApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    private String 맥북_userAgent_헤더;
    private String 리프레쉬_토큰_KEY;
    private User 코인_유저;

    @BeforeAll
    void setup() {
        clear();
        맥북_userAgent_헤더 = userFixture.맥북userAgent헤더();
        코인_유저 = userFixture.코인_유저();
        리프레쉬_토큰_KEY = 코인_유저.getId() + ":PC";
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", userFixture.맥북userAgent헤더())
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(
                jsonPath("$.refresh_token").value(refreshTokenRedisRepository.getById(리프레쉬_토큰_KEY).getToken()))
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", 맥북_userAgent_헤더)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode jsonNode = JsonAssertions.convertJsonNode(result);
        mockMvc.perform(
                post("/user/logout")
                    .header("Authorization", "Bearer " + jsonNode.get("token").asText())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", 맥북_userAgent_헤더)
            )
            .andExpect(status().isOk());

        Assertions.assertThat(refreshTokenRedisRepository.findById(리프레쉬_토큰_KEY)).isEmpty();
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", userFixture.맥북userAgent헤더())
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("User-Agent", 맥북_userAgent_헤더)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode refreshJsonNode = JsonAssertions.convertJsonNode(refreshResult);

        String refreshToken = refreshTokenRedisRepository.getById(리프레쉬_토큰_KEY).getToken();

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
