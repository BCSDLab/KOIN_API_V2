package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.support.JsonAssertions;

@SuppressWarnings("NonAsciiCharacters")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthApiTest extends AcceptanceTest {

    @Autowired
    private UserFixture userFixture;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTokenRepository tokenRepository;

    @Test
    @Transactional
    void 사용자가_로그인을_수행한다() throws Exception {
        User user = userFixture.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

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

        User userResult = userRepository.findById(user.getId()).get();
        UserToken token = tokenRepository.findById(userResult.getId()).get();

        JsonAssertions.assertThat(result.getResponse().getContentAsString())
            .isEqualTo(String.format("""
                    {
                        "token": "%s",
                        "refresh_token": "%s",
                        "user_type": "%s"
                    }
                    """,
                jsonNode.get("token").asText(),
                token.getRefreshToken(),
                user.getUserType().name()
            ));
    }

    @Test
    @Transactional
    void 사용자가_로그인_이후_로그아웃을_수행한다() throws Exception {
        User user = userFixture.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

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
    @Transactional
    void 용자가_로그인_이후_refreshToken을_재발급한다() throws Exception {
        User user = userFixture.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

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
