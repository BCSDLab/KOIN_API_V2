package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.acceptance.support.JsonAssertions;

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthApiTest extends AcceptanceTest {

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private UserTokenRedisRepository tokenRepository;

    @Test
    void 사용자가_로그인을_수행한다() throws Exception {
        User user = userFixture.코인_유저();

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
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(
                jsonPath("$.refresh_token").value(tokenRepository.findById(user.getId()).get().getRefreshToken()))
            .andExpect(jsonPath("$.user_type").value(user.getUserType().name()))
            .andReturn();
    }

    @Test
    void 사용자가_로그인_이후_로그아웃을_수행한다() throws Exception {
        User user = userFixture.코인_유저();

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
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode jsonNode = JsonAssertions.convertJsonNode(result);
        mockMvc.perform(
                post("/user/logout")
                    .header("Authorization", "Bearer " + jsonNode.get("token").asText())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        Assertions.assertThat(tokenRepository.findById(user.getId())).isEmpty();
    }

    @Test
    void 사용자가_로그인_이후_refreshToken을_재발급한다() throws Exception {
        User user = userFixture.코인_유저();

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
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode refreshJsonNode = JsonAssertions.convertJsonNode(refreshResult);

        UserToken token = tokenRepository.findById(user.getId()).get();

        JsonAssertions.assertThat(refreshResult.getResponse().getContentAsString())
            .isEqualTo(String.format("""
                    {
                        "token": "%s",
                        "refresh_token": "%s"
                    }
                    """,
                refreshJsonNode.get("token").asText(),
                token.getRefreshToken()
            ));
    }
}
