package in.koreatech.koin.acceptance.domain;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import in.koreatech.koin.global.auth.JwtProvider;
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
    private JwtProvider jwtProvider;

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

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("publicRequests")
    void 공개_GET은_잘못되거나_만료된_인증을_익명으로_처리한다(String path, InvalidCredential credential)
        throws Exception {
        mockMvc.perform(withInvalidCredential(get(path), credential))
            .andExpect(status().isOk());
    }

    static Stream<Arguments> publicRequests() {
        return Stream.of(
                "/depts", "/shops/categories", "/banner-categories", "/clubs", "/articles",
                "/articles/lost-item/v2?author=ALL", "/callvan?author=ALL"
            )
            .flatMap(path -> Stream.of(InvalidCredential.values()).map(credential -> Arguments.of(path, credential)));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("protectedRequests")
    void 필수_인증과_MY_필터는_잘못되거나_만료된_인증을_거부한다(String path, InvalidCredential credential)
        throws Exception {
        mockMvc.perform(withInvalidCredential(get(path), credential))
            .andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> protectedRequests() {
        return Stream.of(
                "/v3/users/me", "/callvan/posts/1", "/callvan/posts/1/chat",
                "/callvan?author=MY", "/articles/lost-item/v2?author=MY"
            )
            .flatMap(path -> Stream.of(InvalidCredential.values()).map(credential -> Arguments.of(path, credential)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/v3/users/me", "/callvan/posts/1", "/callvan/posts/1/chat",
        "/callvan?author=MY", "/articles/lost-item/v2?author=MY"
    })
    void 필수_인증과_MY_필터는_인증이_없어도_거부한다(String path) throws Exception {
        mockMvc.perform(get(path))
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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 정상_인증은_공개_조회와_MY_필터의_개인화를_유지한다(boolean cookieAuthentication) throws Exception {
        CallvanPost callvan = callvanFixture.콜벤팟(user, LocalDateTime.now().plusDays(1));
        Cookie access = cookieAuthentication ? loginCookie() : null;
        String token = userFixture.getToken(user);

        for (String author : new String[] {"ALL", "MY"}) {
            MockHttpServletRequestBuilder request = get("/callvan").param("author", author);
            if (cookieAuthentication) {
                request.cookie(access).header(HttpHeaders.ORIGIN, ORIGIN);
            } else {
                request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
            mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].id").value(callvan.getId()))
                .andExpect(jsonPath("$.posts[0].is_author").value(true))
                .andExpect(jsonPath("$.posts[0].is_joined").value(true));
        }
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

    @Test
    void 기존_로그인은_잘못된_Bearer가_있어도_입력값을_검증한다() throws Exception {
        mockMvc.perform(post("/v2/users/login")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .header(HttpHeaders.USER_AGENT, userFixture.맥북userAgent헤더())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"login_id":"","login_pw":"1234"}
                    """))
            .andExpect(status().isBadRequest());
    }

    private MockHttpServletRequestBuilder withInvalidCredential(
        MockHttpServletRequestBuilder request, InvalidCredential credential
    ) {
        String token = switch (credential) {
            case INVALID_COOKIE, INVALID_BEARER -> "invalid-token";
            case EXPIRED_COOKIE -> jwtProvider.createWebToken(user, "expired-session", Instant.now().minusSeconds(60));
            case EXPIRED_BEARER -> jwtProvider.createWebToken(user, null, Instant.now().minusSeconds(60));
        };
        return switch (credential) {
            case INVALID_COOKIE, EXPIRED_COOKIE -> request.cookie(new Cookie(properties.accessCookieName(), token));
            case INVALID_BEARER, EXPIRED_BEARER -> request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
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
        INVALID_COOKIE, INVALID_BEARER, EXPIRED_COOKIE, EXPIRED_BEARER
    }
}
