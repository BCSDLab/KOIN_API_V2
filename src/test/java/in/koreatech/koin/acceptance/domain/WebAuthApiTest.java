package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.RefreshTokenRedisRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

class WebAuthApiTest extends AcceptanceTest {

    private static final String ORIGIN = "http://localhost:3000";
    private static final String AUTH_PATH = "/v2/web/auth";
    private static final String MOBILE_USER_AGENT = "koin/1.0 (Android 14) OKHttp/4.12.0";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRedisRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private WebAuthProperties properties;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
            .loginId("web-test")
            .loginPw(passwordEncoder.encode("1234"))
            .name("웹인증테스트")
            .nickname("웹인증")
            .phoneNumber("01055556666")
            .email("web-auth@koreatech.ac.kr")
            .userType(UserType.GENERAL)
            .isAuthed(true)
            .isDeleted(false)
            .build());
    }

    @Test
    void 웹은_access와_refresh를_HttpOnly_쿠키로만_발급한다() throws Exception {
        WebLogin login = login(true);

        assertThat(login.access().isHttpOnly()).isTrue();
        assertThat(login.refresh().isHttpOnly()).isTrue();
        assertThat(login.access().getSecure()).isTrue();
        assertThat(login.refresh().getSecure()).isTrue();
        assertThat(login.access().getPath()).isEqualTo("/");
        assertThat(login.refresh().getPath()).isEqualTo(AUTH_PATH);
        assertThat(login.access().getDomain()).isNull();
        assertThat(login.refresh().getDomain()).isNull();
        assertThat(login.access().getMaxAge()).isBetween(1, 900);
        assertThat(login.refresh().getMaxAge()).isBetween(7_775_900, 7_776_000);
        assertThat(login.result().getResponse().getHeaders(HttpHeaders.SET_COOKIE))
            .hasSize(2).allSatisfy(value -> assertThat(value).contains("SameSite=Lax"));
        assertThat(objectMapper.readTree(login.result().getResponse().getContentAsString()).size()).isEqualTo(2);
    }

    @Test
    void 자동로그인을_선택하지_않으면_브라우저_세션_쿠키를_발급한다() throws Exception {
        WebLogin login = login(false);

        assertThat(login.access().getMaxAge()).isEqualTo(-1);
        assertThat(login.refresh().getMaxAge()).isEqualTo(-1);
    }

    @Test
    void 웹_로그인은_UserAgent_없이도_가능하다() throws Exception {
        mockMvc.perform(post(AUTH_PATH + "/login").header("Origin", ORIGIN)
                .contentType(MediaType.APPLICATION_JSON).content(loginBody(false)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user_type").value("GENERAL"));
    }

    @Test
    void 비인증_API는_남아있는_쿠키_때문에_인증이나_csrf를_요구하지_않는다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(post("/v2/users/register").cookie(expiredAccess(login))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v2/users/register").cookie(login.access())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 기존_일반_API를_쿠키로_인증하고_권한_검사를_재사용한다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(get("/v2/users/me").cookie(login.access()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login_id").value("web-test"));
        mockMvc.perform(get("/user/student/me").cookie(login.access()))
            .andExpect(status().isForbidden());
    }

    @Test
    void 모바일웹_로그인과_로그아웃이_앱의_로그인_정보를_덮어쓰지_않는다() throws Exception {
        JsonNode nativeLogin = nativeLogin();
        String nativeRefresh = nativeLogin.get("refresh_token").asText();
        WebLogin webLogin = login(true);

        assertThat(refreshTokenRepository.getById(user.getId() + ":Mobile").getToken()).isEqualTo(nativeRefresh);
        logout(webLogin).andExpect(status().isNoContent());

        mockMvc.perform(post("/user/refresh")
                .header("User-Agent", MOBILE_USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refresh_token", nativeRefresh))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.refresh_token").value(nativeRefresh))
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void 앱_로그아웃은_웹_세션을_폐기하지_않는다() throws Exception {
        JsonNode nativeLogin = nativeLogin();
        WebLogin webLogin = login(true);

        mockMvc.perform(post("/user/logout")
                .header("Authorization", "Bearer " + nativeLogin.get("token").asText())
                .header("User-Agent", MOBILE_USER_AGENT))
            .andExpect(status().isOk());
        mockMvc.perform(get("/user/auth").cookie(webLogin.access())).andExpect(status().isOk());
    }

    @Test
    void 한_브라우저의_로그아웃이_다른_웹_세션을_폐기하지_않는다() throws Exception {
        WebLogin first = login(true);
        WebLogin second = login(true);

        logout(first).andExpect(status().isNoContent());
        mockMvc.perform(get("/user/auth").cookie(first.access())).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/user/auth").cookie(second.access())).andExpect(status().isOk());
    }

    @Test
    void 만료된_access_쿠키가_있어도_csrf_조회와_재발급이_가능하다() throws Exception {
        WebLogin login = login(true);
        Cookie expired = expiredAccess(login);

        mockMvc.perform(get(AUTH_PATH + "/csrf").header("Origin", ORIGIN).cookie(expired, login.refresh()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.csrf_token").value(login.csrfToken()))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));

        mockMvc.perform(post(AUTH_PATH + "/refresh")
                .header("Origin", ORIGIN)
                .header("Authorization", "Bearer expired-migration-header")
                .header("X-CSRF-Token", login.csrfToken())
                .cookie(expired, login.refresh()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.refresh_token").doesNotExist());
    }

    @Test
    void 재발급한_refresh는_교체되고_이전_refresh는_다시_사용할_수_없다() throws Exception {
        WebLogin login = login(true);
        MvcResult refreshed = refresh(login).andExpect(status().isCreated()).andReturn();
        Cookie nextRefresh = refreshed.getResponse().getCookie(properties.refreshCookieName());

        assertThat(nextRefresh.getValue()).isNotEqualTo(login.refresh().getValue());
        assertThat(nextRefresh.getMaxAge()).isLessThanOrEqualTo(login.refresh().getMaxAge());
        refresh(login).andExpect(status().isUnauthorized()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        mockMvc.perform(post(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
                .header("X-CSRF-Token", login.csrfToken()).cookie(nextRefresh))
            .andExpect(status().isCreated());
    }

    @Test
    void 상태_변경에는_origin과_세션에_맞는_csrf가_필요하다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(put("/users/password").header("Origin", ORIGIN).cookie(login.access())
                .contentType(MediaType.APPLICATION_JSON).content("{\"new_password\":\"changed-password\"}"))
            .andExpect(status().isForbidden());
        assertThat(passwordEncoder.matches("1234", user.getLoginPw())).isTrue();

        mockMvc.perform(put("/users/password").header("Origin", ORIGIN).cookie(login.access())
                .header("X-CSRF-Token", login.csrfToken())
                .contentType(MediaType.APPLICATION_JSON).content("{\"new_password\":\"changedpassword\"}"))
            .andExpect(status().isOk());
        assertThat(passwordEncoder.matches("changedpassword", user.getLoginPw())).isTrue();
        refresh(login).andExpect(status().isUnauthorized());
    }

    @Test
    void 다른_웹_세션의_csrf_토큰은_사용할_수_없다() throws Exception {
        WebLogin first = login(true);
        WebLogin second = login(true);

        mockMvc.perform(post(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
                .header("X-CSRF-Token", second.csrfToken()).cookie(first.refresh()))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        refresh(first).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "https://evil.example", "http://localhost.evil.example:3000"})
    void 허용되지_않은_출처의_로그인은_거부한다(String origin) throws Exception {
        mockMvc.perform(post(AUTH_PATH + "/login").header("Origin", origin)
                .contentType(MediaType.APPLICATION_JSON).content(loginBody(true)))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void 출처가_없는_로그인은_거부한다() throws Exception {
        mockMvc.perform(post(AUTH_PATH + "/login")
                .contentType(MediaType.APPLICATION_JSON).content(loginBody(true)))
            .andExpect(status().isForbidden());
    }

    @Test
    void 로그인은_브라우저의_단순_form_요청을_받지_않는다() throws Exception {
        mockMvc.perform(post(AUTH_PATH + "/login").header("Origin", ORIGIN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).content("login_id=web-test&login_pw=1234"))
            .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void 잘못된_비밀번호로_쿠키를_발급하지_않는다() throws Exception {
        mockMvc.perform(post(AUTH_PATH + "/login").header("Origin", ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login_id\":\"web-test\",\"login_pw\":\"wrong\"}"))
            .andExpect(status().isBadRequest()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void 로그아웃은_만료된_access와_무관하며_쿠키와_웹_세션을_폐기한다() throws Exception {
        WebLogin login = login(true);

        MvcResult result = mockMvc.perform(post(AUTH_PATH + "/logout").header("Origin", ORIGIN)
                .header("X-CSRF-Token", login.csrfToken()).cookie(expiredAccess(login), login.refresh()))
            .andExpect(status().isNoContent()).andReturn();

        Cookie access = result.getResponse().getCookie(properties.accessCookieName());
        Cookie refresh = result.getResponse().getCookie(properties.refreshCookieName());
        assertThat(access.getMaxAge()).isZero();
        assertThat(refresh.getMaxAge()).isZero();
        assertThat(access.getPath()).isEqualTo(login.access().getPath());
        assertThat(refresh.getPath()).isEqualTo(login.refresh().getPath());
        assertThat(access.isHttpOnly()).isTrue();
        assertThat(refresh.getSecure()).isTrue();
        mockMvc.perform(get("/user/auth").cookie(login.access())).andExpect(status().isUnauthorized());
        refresh(login).andExpect(status().isUnauthorized());
        logout(login).andExpect(status().isNoContent());
    }

    @Test
    void csrf가_없는_로그아웃은_쿠키나_세션을_삭제하지_않는다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(post(AUTH_PATH + "/logout").header("Origin", ORIGIN).cookie(login.refresh()))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        mockMvc.perform(get("/user/auth").cookie(login.access())).andExpect(status().isOk());
    }

    @Test
    void 웹_쿠키로_기존_앱_로그아웃을_호출할_수_없다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(post("/user/logout").header("User-Agent", MOBILE_USER_AGENT).cookie(login.access()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 웹_토큰을_앱_인증이나_앱_재발급으로_바꿔_사용할_수_없다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(get("/user/auth").header("Authorization", "Bearer " + login.access().getValue()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/user/refresh").header("User-Agent", MOBILE_USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refresh_token", login.refresh().getValue()))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 앱_토큰을_웹_쿠키로_사용할_수_없다() throws Exception {
        JsonNode nativeLogin = nativeLogin();

        mockMvc.perform(get("/user/auth")
                .cookie(new Cookie(properties.accessCookieName(), nativeLogin.get("token").asText())))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
                .header("X-CSRF-Token", "any")
                .cookie(new Cookie(properties.refreshCookieName(), nativeLogin.get("refresh_token").asText())))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 웹_재발급은_본문으로_전달한_토큰을_사용하지_않는다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(post(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
                .header("X-CSRF-Token", login.csrfToken()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("refresh_token", login.refresh().getValue()))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 명시적_헤더가_있으면_쿠키_사용자로_대체하지_않는다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(get("/user/auth").header("Authorization", "invalid").cookie(login.access()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/user/auth").header("Authorization", "Bearer " + jwtProvider.createToken(user))
                .cookie(new Cookie(properties.accessCookieName(), "invalid-cookie")))
            .andExpect(status().isOk());
    }

    @Test
    void 중복된_인증_쿠키를_임의로_선택하지_않는다() throws Exception {
        WebLogin login = login(true);

        mockMvc.perform(get("/user/auth").cookie(login.access(), new Cookie(properties.accessCookieName(), "another")))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
                .header("X-CSRF-Token", login.csrfToken())
                .cookie(login.refresh(), new Cookie(properties.refreshCookieName(), "another")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 허용된_웹의_쿠키와_csrf_헤더에_대한_CORS_preflight를_허용한다() throws Exception {
        mockMvc.perform(options(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-CSRF-Token"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    private WebLogin login(boolean autoLogin) throws Exception {
        MvcResult result = mockMvc.perform(post(AUTH_PATH + "/login").header("Origin", ORIGIN)
                .header("User-Agent", MOBILE_USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON).content(loginBody(autoLogin)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.user_type").value("GENERAL"))
            .andExpect(jsonPath("$.csrf_token").isNotEmpty())
            .andExpect(jsonPath("$.token").doesNotExist())
            .andExpect(jsonPath("$.refresh_token").doesNotExist())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andReturn();
        return new WebLogin(result.getResponse().getCookie(properties.accessCookieName()),
            result.getResponse().getCookie(properties.refreshCookieName()),
            objectMapper.readTree(result.getResponse().getContentAsString()).get("csrf_token").asText(), result);
    }

    private JsonNode nativeLogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/v2/users/login").header("User-Agent", MOBILE_USER_AGENT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login_id\":\"web-test\",\"login_pw\":\"1234\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.refresh_token").isNotEmpty())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String loginBody(boolean autoLogin) {
        return """
            {"login_id":"web-test","login_pw":"1234","auto_login":%s}
            """.formatted(autoLogin);
    }

    private ResultActions refresh(WebLogin login) throws Exception {
        return mockMvc.perform(post(AUTH_PATH + "/refresh").header("Origin", ORIGIN)
            .header("X-CSRF-Token", login.csrfToken()).cookie(login.refresh()));
    }

    private ResultActions logout(WebLogin login) throws Exception {
        return mockMvc.perform(post(AUTH_PATH + "/logout").header("Origin", ORIGIN)
            .header("X-CSRF-Token", login.csrfToken()).cookie(login.refresh()));
    }

    private Cookie expiredAccess(WebLogin login) {
        String sessionId = WebRefreshToken.parse(login.refresh().getValue()).sessionId();
        return new Cookie(properties.accessCookieName(), jwtProvider.createWebToken(user, sessionId, Instant.now().minusSeconds(1)));
    }

    private record WebLogin(Cookie access, Cookie refresh, String csrfToken, MvcResult result) {

    }
}
