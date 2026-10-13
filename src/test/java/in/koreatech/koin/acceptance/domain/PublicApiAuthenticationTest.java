package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.CallvanAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

@SuppressWarnings("NonAsciiCharacters")
class PublicApiAuthenticationTest extends AcceptanceTest {

    private static final String ORIGIN = "http://localhost:3000";
    private static final String LOGIN_BODY = """
        {"login_id":"public-auth-user","login_pw":"1234"}
        """;

    @Autowired
    private UserAcceptanceFixture userFixture;

    @Autowired
    private CallvanAcceptanceFixture callvanFixture;

    @Autowired
    private WebAuthProperties properties;

    private User user;

    @BeforeAll
    void clearDatabase() {
        clear();
    }

    @BeforeEach
    void setUp() {
        user = callvanFixture.콜벤_유저("public-auth-user", "공개API인증");
    }

    @Test
    void 인증정보를_사용하지_않는_공개_GET은_잘못된_Bearer가_있어도_조회한다() throws Exception {
        mockMvc.perform(withInvalidCredential(get("/depts"), InvalidCredential.INVALID_BEARER))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @EnumSource(InvalidCredential.class)
    void 인터페이스에만_선언된_필수_인증도_잘못된_인증정보를_거부한다(InvalidCredential credential)
        throws Exception {
        mockMvc.perform(withInvalidCredential(get("/callvan/posts/1/chat"), credential))
            .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/callvan?author=MY", "/articles/lost-item/v2?author=MY"})
    void MY_필터는_비로그인으로_전환된_요청을_거부한다(String path) throws Exception {
        mockMvc.perform(withInvalidCredential(get(path), InvalidCredential.INVALID_BEARER))
            .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(InvalidCredential.class)
    void 잘못된_인증으로는_콜밴_작성자와_참여자로_표시하지_않는다(InvalidCredential credential) throws Exception {
        CallvanPost callvan = callvanFixture.콜벤팟(user, LocalDateTime.now().plusDays(1));

        mockMvc.perform(withInvalidCredential(get("/callvan").param("author", "ALL"), credential))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts[0].id").value(callvan.getId()))
            .andExpect(jsonPath("$.posts[0].is_author").value(false))
            .andExpect(jsonPath("$.posts[0].is_joined").value(false));
    }

    @Test
    void 공개_GET도_정상_쿠키의_Origin_검증은_생략하지_않는다() throws Exception {
        mockMvc.perform(get("/callvan").cookie(loginCookie()))
            .andExpect(status().isForbidden());
    }

    @Test
    void 기존_로그인은_잘못된_Bearer가_있어도_정상_자격으로_로그인한다() throws Exception {
        mockMvc.perform(post("/v2/users/login")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .header(HttpHeaders.USER_AGENT, userFixture.맥북userAgent헤더())
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    private MockHttpServletRequestBuilder withInvalidCredential(
        MockHttpServletRequestBuilder request, InvalidCredential credential
    ) {
        return switch (credential) {
            case INVALID_COOKIE -> request.cookie(new Cookie(properties.accessCookieName(), "invalid-token"));
            case INVALID_BEARER -> request.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token");
        };
    }

    private Cookie loginCookie() throws Exception {
        return mockMvc.perform(post("/v2/web/auth/login")
                .header(HttpHeaders.ORIGIN, ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(LOGIN_BODY))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getCookie(properties.accessCookieName());
    }

    enum InvalidCredential {
        INVALID_COOKIE, INVALID_BEARER
    }
}
