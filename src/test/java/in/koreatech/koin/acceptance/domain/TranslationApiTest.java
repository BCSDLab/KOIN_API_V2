package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.TranslationResource;
import in.koreatech.koin.domain.translation.model.TranslationType;
import in.koreatech.koin.domain.translation.service.*;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.infrastructure.upstage.client.UpstageProperties;
import in.koreatech.koin.infrastructure.upstage.client.UpstageTranslationClient;

/** 실제 MVC·저장소를 사용한다. 원문은 합성 데이터이며 정상 호출은 로컬 HTTP 서버를 거친다. */
@Transactional(propagation = NOT_SUPPORTED)
@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key=", "translation.cleanup-cron=-"})
class TranslationApiTest extends AcceptanceTest {
    private static final String SUMMARY = "/v2/articles/17/ai-summary/translations";
    private static final String LOST = "/articles/lost-item/17/translations";
    private static final String LOST_MESSAGE = "/v2/chatroom/lost-item/17/2/messages/733900000000012345/translations";

    @MockBean TranslationAiClient aiClient;
    @MockBean TranslationSourceReader sourceReader;
    @Autowired JdbcTemplate jdbc;
    @Autowired TranslationProperties properties;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository users;
    @Autowired JwtProvider jwtProvider;
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

    @Test
    void 번역_중에는_기존_작업을_조회하고_완료_후에는_저장된_번역을_재사용한다() throws Exception {
        var resource = TranslationResource.article(TranslationType.ARTICLE_SUMMARY, 17);
        var source = TranslationSource.of(resource,
            TranslationSourceFields.summary(List.of(Map.of("icon", "📅", "text", "신청은 금요일까지입니다."))),
            Map.of("item_0", "신청은 금요일까지입니다."), null);
        when(sourceReader.load(resource, null)).thenReturn(source);

        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var calls = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        String translated = "Applications close on Friday.";
        byte[] response = objectMapper.writeValueAsBytes(Map.of("choices", List.of(Map.of(
            "finish_reason", "stop", "message", Map.of("content", objectMapper.writeValueAsString(Map.of("item_0", translated)))))));
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            calls.incrementAndGet();
            entered.countDown();
            try {
                if (release.await(10, TimeUnit.SECONDS)) {
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        var upstage = new UpstageProperties();
        upstage.setApiKey("translation-test-key");
        upstage.setApiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
        var client = new UpstageTranslationClient(objectMapper, upstage, properties);
        when(aiClient.translate(source.texts(), SupportedLanguage.ENGLISH)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return client.translate(source.texts(), SupportedLanguage.ENGLISH);
        });
        var executor = Executors.newSingleThreadExecutor();
        try {
            var first = executor.submit(() -> mockMvc.perform(post(SUMMARY)
                    .contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items[0].icon").value("📅"))
                .andExpect(jsonPath("$.data.items[0].text").value(translated)).andReturn());
            assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue();
            var pending = mockMvc.perform(post(SUMMARY)
                    .contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isAccepted()).andExpect(header().string(HttpHeaders.RETRY_AFTER, "2"))
                .andExpect(jsonPath("$.status").value("PENDING")).andReturn();
            String id = JsonAssertions.convertJsonNode(pending).path("translation_id").asText();
            mockMvc.perform(get("/translations/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
            release.countDown();
            assertThat(JsonAssertions.convertJsonNode(first.get(10, TimeUnit.SECONDS)).path("translation_id").asText())
                .isEqualTo(id);
            mockMvc.perform(post(SUMMARY).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.translation_id").value(id))
                .andExpect(jsonPath("$.data.items[0].text").value(translated));
            mockMvc.perform(get("/translations/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].text").value(translated))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"));
            assertThat(calls.get()).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM content_translations WHERE status = 'SUCCESS'", Integer.class))
                .isEqualTo(1);
        } finally {
            release.countDown();
            executor.shutdownNow();
            server.stop(0);
        }
    }

    @Test
    void 저장된_쪽지_번역도_현재_로그인과_방_권한을_다시_검사한다() throws Exception {
        var user = users.save(User.builder().userType(UserType.STUDENT).name("번역 테스트")
            .loginPw("unused-test-password").isAuthed(true).isDeleted(false).build());
        createdUsers.add(user);
        String bearer = "Bearer " + jwtProvider.createToken(user);
        var resource = TranslationResource.message(TranslationType.LOST_ITEM_MESSAGE, 17, 2, "733900000000012345");
        var source = TranslationSource.of(resource, TranslationSourceFields.message("안녕하세요"), Map.of("content", "안녕하세요"), null);
        when(sourceReader.load(resource, user.getId())).thenReturn(source);
        when(aiClient.translate(source.texts(), SupportedLanguage.ENGLISH)).thenReturn(Map.of("content", "Hello"));
        var result = mockMvc.perform(post(LOST_MESSAGE).header(HttpHeaders.AUTHORIZATION, bearer)
                .contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("Hello")).andReturn();
        String id = JsonAssertions.convertJsonNode(result).path("translation_id").asText();
        when(sourceReader.load(resource, null)).thenThrow(AuthenticationException.withDetail("login required"));
        mockMvc.perform(get("/translations/{id}", id)).andExpect(status().isUnauthorized());
        when(sourceReader.load(resource, user.getId())).thenThrow(AuthorizationException.withDetail("access revoked"));
        mockMvc.perform(get("/translations/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer))
            .andExpect(status().isForbidden());
        verify(aiClient, times(1)).translate(anyMap(), any());
    }

    @Test
    void 번역_도중_원문이_수정되면_이전_번역을_저장하거나_반환하지_않는다() throws Exception {
        var resource = TranslationResource.article(TranslationType.LOST_ITEM, 17);
        var source = TranslationSource.of(resource, TranslationSourceFields.lostItem("학생회관", null),
            Map.of("found_place", "학생회관"), null);
        var changed = TranslationSource.of(resource, TranslationSourceFields.lostItem("도서관", null),
            Map.of("found_place", "도서관"), null);
        when(sourceReader.load(resource, null)).thenReturn(source);
        when(aiClient.translate(source.texts(), SupportedLanguage.ENGLISH)).thenAnswer(invocation -> {
            when(sourceReader.load(resource, null)).thenReturn(changed);
            return Map.of("found_place", "Student Union");
        });
        mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SOURCE_CHANGED"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM content_translations WHERE translated_json IS NOT NULL", Integer.class))
            .isZero();
        String id = jdbc.queryForObject("SELECT id FROM content_translations", String.class);
        mockMvc.perform(get("/translations/{id}", id))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SOURCE_CHANGED"));
        verify(aiClient, times(1)).translate(anyMap(), any());
    }

    private String body(String version) {
        return "{\"target_language\":\"en-us\",\"expected_source_version\":\"" + version + "\"}";
    }
}
