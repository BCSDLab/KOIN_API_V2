package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.*;
import in.koreatech.koin.domain.translation.service.*;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

/** HTTP/auth/serialization and real translation-service/store integration; domain source policies have separate tests. */
@Transactional(propagation = NOT_SUPPORTED)
@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key=", "translation.cleanup-cron=-"})
class TranslationApiTest extends AcceptanceTest {
    private static final String LOST = "/articles/lost-item/17/translations";
    private static final String LOST_MESSAGE = "/v2/chatroom/lost-item/17/2/messages/733900000000012345/translations";
    private static final String TEAM_MESSAGE = "/chatroom/team-recruitment/17/2/messages/3/translations";
    @MockBean TranslationAiClient aiClient;
    @MockBean TranslationSourceReader sourceReader;
    @Autowired JdbcTemplate jdbc;
    @Autowired TranslationStore store;
    @Autowired TranslationProperties properties;
    @Autowired UserRepository users;
    @Autowired JwtProvider jwtProvider;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired WebAuthProperties webAuthProperties;
    private final List<User> createdUsers = new ArrayList<>();

    @BeforeEach
    void prepare() {
        jdbc.update("DELETE FROM content_translations");
    }

    @AfterEach
    void clean() {
        jdbc.update("DELETE FROM content_translations");
        createdUsers.forEach(users::delete);
        createdUsers.clear();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/v2/articles/17/ai-summary/translations", LOST, "/team-recruitments/17/translations"})
    void publicRequestsBindSerializeCacheAndGetWithoutAnotherSolarCall(String path) throws Exception {
        TranslationType type = path.contains("ai-summary") ? TranslationType.ARTICLE_SUMMARY
            : path.contains("lost-item") ? TranslationType.LOST_ITEM : TranslationType.RECRUITMENT;
        var source = stub(TranslationResource.article(type, 17), null);
        var first = mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isOk()).andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.target_language").value("en-US"))
            .andExpect(jsonPath("$.source_version").value(source.version())).andReturn();
        String id = JsonAssertions.convertJsonNode(first).path("translation_id").asText();
        assertThat(id).isNotBlank();
        if (type == TranslationType.ARTICLE_SUMMARY) {
            assertThat(JsonAssertions.convertJsonNode(first).at("/data/items/0/icon").asText()).isEqualTo("📅");
            assertThat(JsonAssertions.convertJsonNode(first).at("/data/items/0/text").asText()).isEqualTo("Translated");
        }
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.translation_id").value(id));
        mockMvc.perform(get("/translations/{id}", id)).andExpect(status().isOk())
            .andExpect(jsonPath("$.translation_id").value(id))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
        verify(aiClient, times(1)).translate(anyMap(), eq(SupportedLanguage.ENGLISH));
    }

    @Test
    void activeClaimReturns202WithRetryAfterAndGetDoesNotStartSolar() throws Exception {
        var source = stubSource(TranslationResource.article(TranslationType.LOST_ITEM, 17), null);
        var claim = store.claim(source.resource(), source.version(), SupportedLanguage.ENGLISH,
            properties.getModel(), properties.getPromptVersion());
        mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isAccepted()).andExpect(header().string(HttpHeaders.RETRY_AFTER, "2"))
            .andExpect(jsonPath("$.status").value("PENDING"));
        mockMvc.perform(get("/translations/{id}", claim.translation().id()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
        verifyNoInteractions(aiClient);
    }

    @Test
    void unknownResultReturns404WithoutReadingSourceOrCallingSolar() throws Exception {
        mockMvc.perform(get("/translations/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("TRANSLATION_NOT_FOUND"));
        verifyNoInteractions(aiClient, sourceReader);
    }

    @Test
    void oldSourceVersionReturns409WithoutSolar() throws Exception {
        stubSource(TranslationResource.article(TranslationType.LOST_ITEM, 17), null);
        mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body("a".repeat(64))))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SOURCE_CHANGED"));
        verifyNoInteractions(aiClient);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"target_language\":\"en-US\"}",
        "{\"target_language\":\"en-US\",\"expected_source_version\":\"invalid\"}"})
    void malformedRequestsFailBeforeSourceRead(String body) throws Exception {
        mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(aiClient, sourceReader);
    }

    @ParameterizedTest
    @ValueSource(strings = {LOST_MESSAGE, TEAM_MESSAGE})
    void privatePostRequiresLogin(String path) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body("a".repeat(64))))
            .andExpect(status().isUnauthorized());
        verifyNoInteractions(aiClient, sourceReader);
    }

    @Test
    void generalUserCannotTranslateTeamMessages() throws Exception {
        var user = user(UserType.GENERAL);
        mockMvc.perform(post(TEAM_MESSAGE).header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content(body("a".repeat(64))))
            .andExpect(status().isForbidden());
        verifyNoInteractions(aiClient, sourceReader);
    }

    @ParameterizedTest
    @ValueSource(strings = {LOST_MESSAGE, TEAM_MESSAGE})
    void privateRoutesPreserveMessageIdAndResultRechecksAccess(String path) throws Exception {
        var user = user(UserType.STUDENT);
        var resource = path.equals(LOST_MESSAGE)
            ? TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 17, 2, "733900000000012345")
            : TranslationResource.message(TranslationType.RECRUITMENT_MESSAGE, 17, 2, "3");
        var source = stub(resource, user.getId());
        var result = mockMvc.perform(post(path).header(HttpHeaders.AUTHORIZATION, bearer(user))
                .contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("Translated")).andReturn();
        String id = JsonAssertions.convertJsonNode(result).path("translation_id").asText();
        when(sourceReader.load(resource, null)).thenThrow(AuthenticationException.withDetail("login required"));
        mockMvc.perform(get("/translations/{id}", id)).andExpect(status().isUnauthorized());
        when(sourceReader.load(resource, user.getId())).thenThrow(AuthorizationException.withDetail("access revoked"));
        mockMvc.perform(get("/translations/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(user)))
            .andExpect(status().isForbidden());
        verify(aiClient, times(1)).translate(anyMap(), any());
    }

    @Test
    void cookiePostRequiresCsrfBeforeInvokingTranslation() throws Exception {
        var user = user(UserType.GENERAL);
        var login = mockMvc.perform(post("/v2/web/auth/login").header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login_id\":\"" + user.getLoginId() + "\",\"login_pw\":\"1234\"}"))
            .andExpect(status().isCreated()).andReturn();
        Cookie access = login.getResponse().getCookie(webAuthProperties.accessCookieName());
        String csrf = JsonAssertions.convertJsonNode(login).path("csrf_token").asText();
        var source = stub(TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 17, 2, "733900000000012345"), user.getId());
        mockMvc.perform(post(LOST_MESSAGE).cookie(access).header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isForbidden());
        verifyNoInteractions(aiClient);
        mockMvc.perform(post(LOST_MESSAGE).cookie(access).header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header("X-CSRF-Token", csrf).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isOk());
    }

    private TranslationSource stub(TranslationResource resource, Integer userId) {
        var source = stubSource(resource, userId);
        when(aiClient.translate(source.texts(), SupportedLanguage.ENGLISH)).thenAnswer(invocation -> {
            assertThat(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive())
                .as("Solar 호출 동안 DB 트랜잭션을 유지하지 않는다")
                .isFalse();
            return source.texts().keySet().stream()
                .collect(java.util.stream.Collectors.toMap(key -> key, key -> "Translated"));
        });
        return source;
    }

    private TranslationSource stubSource(TranslationResource resource, Integer userId) {
        Map<String, Object> data = switch (resource.type()) {
            case ARTICLE_SUMMARY -> TranslationSourceFields.summary(List.of(Map.of("icon", "📅", "text", "마감")));
            case LOST_ITEM -> TranslationSourceFields.lostItem(null, "분실물");
            case RECRUITMENT -> TranslationSourceFields.recruitment("모집", "설명", null);
            default -> TranslationSourceFields.message("안녕하세요");
        };
        Map<String, String> texts = switch (resource.type()) {
            case ARTICLE_SUMMARY -> Map.of("item_0", "마감");
            case RECRUITMENT -> Map.of("title", "모집", "description", "설명");
            case LOST_ITEM -> Map.of("content", "분실물");
            default -> Map.of("content", "안녕하세요");
        };
        var source = TranslationSource.of(resource, data, texts, null);
        when(sourceReader.load(resource, userId)).thenReturn(source);
        return source;
    }

    private String body(String version) {
        return "{\"target_language\":\"en-us\",\"expected_source_version\":\"" + version + "\"}";
    }

    private User user(UserType type) {
        var user = users.save(User.builder().userType(type).name("번역 테스트")
            .loginId("translation-test-" + java.util.UUID.randomUUID().toString().substring(0, 8))
            .loginPw(passwordEncoder.encode("1234")).isAuthed(true).isDeleted(false).build());
        createdUsers.add(user);
        return user;
    }

    private String bearer(User user) {
        return "Bearer " + jwtProvider.createToken(user);
    }
}
