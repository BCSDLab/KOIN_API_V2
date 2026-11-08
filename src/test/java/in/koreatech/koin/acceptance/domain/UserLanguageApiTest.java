package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key="})
class UserLanguageApiTest extends AcceptanceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebAuthProperties webAuthProperties;

    @BeforeAll
    void clearDatabase() {
        clear();
    }

    @ParameterizedTest
    @EnumSource(value = UserType.class, names = {"GENERAL", "STUDENT", "COUNCIL"})
    void 미설정_계정은_한국어를_조회하고_DB에는_쓰지_않는다(UserType type) throws Exception {
        User user = createUser(type, "default-language");
        entityManager.flush();
        entityManager.clear();
        var updatedAt = userRepository.getById(user.getId()).getUpdatedAt();

        mockMvc.perform(get("/users/me/language").header(HttpHeaders.AUTHORIZATION, bearer(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.language").value("ko-KR"));

        entityManager.flush();
        entityManager.clear();
        User stored = userRepository.getById(user.getId());
        assertThat(stored.getPreferredLanguage()).isNull();
        assertThat(stored.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @ParameterizedTest
    @EnumSource(value = UserType.class, names = {"GENERAL", "STUDENT", "COUNCIL"})
    void 본인_언어만_정규화해_저장하고_다시_조회한다(UserType type) throws Exception {
        User user = createUser(type, "language-owner");
        User other = createUser(type, "language-other");
        String token = bearer(user);

        mockMvc.perform(patch("/users/me/language")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"language\":\"en-us\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.language").value("en-US"));

        entityManager.flush();
        entityManager.clear();
        User stored = userRepository.getById(user.getId());
        assertThat(stored.getPreferredLanguage()).isEqualTo("en-US");
        assertThat(stored.getName()).isEqualTo("기존이름");
        assertThat(stored.getNickname()).isEqualTo("language-owner");
        assertThat(stored.getLoginPw()).isEqualTo("test-password");
        assertThat(userRepository.getById(other.getId()).getPreferredLanguage()).isNull();

        mockMvc.perform(get("/users/me/language").header(HttpHeaders.AUTHORIZATION, token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.language").value("en-US"));
        mockMvc.perform(patch("/users/me/language")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"language\":\"en-US\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.language").value("en-US"));
    }

    @Test
    void 지원하지_않는_언어는_기존_설정을_보존하고_400을_반환한다() throws Exception {
        User user = createUser(UserType.GENERAL, "unsupported-language");
        user.changeLanguage(SupportedLanguage.JAPANESE);

        mockMvc.perform(patch("/users/me/language")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"language\":\"fr-FR\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("UNSUPPORTED_LANGUAGE"));

        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.getById(user.getId()).getPreferredLanguage()).isEqualTo("ja-JP");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 웹_쿠키로_언어를_변경할_때_CSRF를_검증하고_실제_커밋한_값을_다른_요청에서_조회한다() throws Exception {
        User user = userRepository.save(User.builder()
            .userType(UserType.GENERAL).loginId("language-web-user").name("기존이름")
            .loginPw(passwordEncoder.encode("1234")).isAuthed(true).isDeleted(false).build());
        try {
            var login = mockMvc.perform(post("/v2/web/auth/login")
                    .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"login_id\":\"language-web-user\",\"login_pw\":\"1234\"}"))
                .andExpect(status().isCreated()).andReturn();
            Cookie access = login.getResponse().getCookie(webAuthProperties.accessCookieName());
            String csrf = JsonAssertions.convertJsonNode(login).path("csrf_token").asText();

            mockMvc.perform(patch("/users/me/language").cookie(access)
                    .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                    .contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"en-US\"}"))
                .andExpect(status().isForbidden());
            assertThat(userRepository.getById(user.getId()).getPreferredLanguage()).isNull();

            mockMvc.perform(patch("/users/me/language").cookie(access)
                    .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                    .header("X-CSRF-Token", csrf)
                    .contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"en-US\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.language").value("en-US"));

            assertThat(userRepository.getById(user.getId()).getPreferredLanguage()).isEqualTo("en-US");
            mockMvc.perform(get("/users/me/language").header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
                .andExpect(jsonPath("$.language").value("en-US"));
        } finally {
            userRepository.delete(userRepository.getById(user.getId()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"language\":null}", "{\"language\":\"\"}", "{\"language\":\" \"}",
        "{\"language\":17}", "{\"language\":true}", "{\"language\":{}}", "{\"language\":[]}"})
    void 잘못된_입력은_기존_설정을_보존한다(String body) throws Exception {
        User user = createUser(UserType.GENERAL, "invalid-language");
        user.changeLanguage(SupportedLanguage.ENGLISH);

        mockMvc.perform(patch("/users/me/language")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());

        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.getById(user.getId()).getPreferredLanguage()).isEqualTo("en-US");
    }

    @Test
    void 지원이_끝난_저장값은_한국어로_조회하되_덮어쓰지_않는다() throws Exception {
        User user = createUser(UserType.GENERAL, "retired-language");
        entityManager.flush();
        entityManager.createNativeQuery("UPDATE users SET preferred_language = 'retired-code' WHERE id = :id")
            .setParameter("id", user.getId()).executeUpdate();
        entityManager.clear();

        mockMvc.perform(get("/users/me/language").header(HttpHeaders.AUTHORIZATION, bearer(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.language").value("ko-KR"));

        entityManager.flush();
        entityManager.clear();
        assertThat(userRepository.getById(user.getId()).getPreferredLanguage()).isEqualTo("retired-code");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer invalid-token"})
    void 인증되지_않으면_조회와_변경을_거절한다(String authorization) throws Exception {
        mockMvc.perform(get("/users/me/language").header(HttpHeaders.AUTHORIZATION, authorization))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/users/me/language")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"en-US\"}"))
            .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @EnumSource(value = UserType.class, names = {"OWNER", "COOP", "ADMIN"})
    void 허용되지_않은_회원_유형은_조회와_변경을_거절한다(UserType type) throws Exception {
        User user = createUser(type, "forbidden-language");
        mockMvc.perform(get("/users/me/language").header(HttpHeaders.AUTHORIZATION, bearer(user)))
            .andExpect(status().isForbidden());
        mockMvc.perform(patch("/users/me/language")
                .header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content("{\"language\":\"en-US\"}"))
            .andExpect(status().isForbidden());
        assertThat(user.getPreferredLanguage()).isNull();
    }

    private User createUser(UserType type, String nickname) {
        return userRepository.save(User.builder()
            .userType(type).nickname(nickname).name("기존이름").loginPw("test-password")
            .isAuthed(true).isDeleted(false).build());
    }

    private String bearer(User user) {
        return "Bearer " + jwtProvider.createToken(user);
    }
}
