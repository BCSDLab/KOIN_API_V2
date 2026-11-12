package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
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
import in.koreatech.koin.domain.translation.model.TranslationReason;
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
    @Autowired TranslationStore store;
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
            // 프롬프트 교체 전 저장된 혼합 언어 결과는 ID로 직접 조회해도 반환하지 않는다.
            var legacy = store.claim(resource, source.version(), SupportedLanguage.ENGLISH, properties.getModel(), "v2");
            store.succeed(legacy.translation().id(), legacy.ownerToken(), objectMapper.writeValueAsString(
                Map.of("items", List.of(Map.of("icon", "📅", "text", "Applications close ましょう.")))));
            mockMvc.perform(get("/translations/{id}", legacy.translation().id()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.reason_code").value("INVALID_TRANSLATION"))
                .andExpect(jsonPath("$.retryable").value(true)).andExpect(jsonPath("$.data").isEmpty());
            assertThat(store.findById(legacy.translation().id()).orElseThrow().translatedJson()).isNull();

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
            assertThat(id).isNotEqualTo(legacy.translation().id());
            assertThat(store.findById(id).orElseThrow().promptVersion()).isEqualTo(properties.getPromptVersion());
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

            String savedJson = jdbc.queryForObject("SELECT translated_json FROM content_translations WHERE id = ?",
                String.class, id);
            assertThat(store.invalidateSuccess(id, savedJson.replace("Applications", "APPLICATIONS"))).isFalse();
            assertThat(store.findById(id).orElseThrow().translatedJson()).isEqualTo(savedJson);

            String originalJson = objectMapper.writeValueAsString(source.data());
            jdbc.update("UPDATE content_translations SET translated_json = ? WHERE id = ?", originalJson, id);
            mockMvc.perform(get("/translations/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.reason_code").value("INVALID_TRANSLATION"))
                .andExpect(jsonPath("$.retryable").value(true)).andExpect(jsonPath("$.data").isEmpty());
            mockMvc.perform(post(SUMMARY).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.translation_id").value(id))
                .andExpect(jsonPath("$.data.items[0].text").value(translated));

            jdbc.update("UPDATE content_translations SET translated_json = ? WHERE id = ?", originalJson, id);
            mockMvc.perform(post(SUMMARY).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.reason_code").value("INVALID_TRANSLATION"));
            mockMvc.perform(post(SUMMARY).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.translation_id").value(id))
                .andExpect(jsonPath("$.data.items[0].text").value(translated));
            assertThat(calls.get()).isEqualTo(3);
        } finally {
            release.countDown();
            executor.shutdownNow();
            server.stop(0);
        }

        // 새 콘텐츠 경로도 같은 MVC·DB 재사용 계약을 통과해야 한다.
        record Example(TranslationSource source, String path, Map<String, String> translated, String field, String expected) {}
        var examples = List.of(
            new Example(TranslationSource.of(TranslationResource.article(TranslationType.ARTICLE_CONTENT, 18),
                TranslationSourceFields.message("<p>안녕하세요</p>"), Map.of("text_0", "안녕하세요"), null),
                "/v2/articles/18/translations", Map.of("text_0", "Hello"), "content", "<p>Hello</p>"),
            new Example(TranslationSource.of(new TranslationResource(TranslationType.ARTICLE_COMMENT, "19", 18, 0),
                TranslationSourceFields.message("안녕하세요"), Map.of("content", "안녕하세요"), null),
                "/articles/18/comments/19/translations", Map.of("content", "Hello"), "content", "Hello"),
            new Example(TranslationSource.of(TranslationResource.article(TranslationType.CALLVAN_POST, 20),
                TranslationSourceFields.callvanPost("정문 -> 천안역", "정문", "천안역"), Map.of("departure", "정문"), null),
                "/callvan/posts/20/translations", Map.of("departure", "Main gate"), "title", "Main gate -> Cheonan Station")
        );
        for (var example : examples) {
            when(sourceReader.load(example.source().resource(), null)).thenReturn(example.source());
            when(aiClient.translate(example.source().texts(), SupportedLanguage.ENGLISH)).thenReturn(example.translated());
            var first = mockMvc.perform(post(example.path()).contentType(MediaType.APPLICATION_JSON)
                    .content(body(example.source().version())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data." + example.field()).value(example.expected())).andReturn();
            String id = JsonAssertions.convertJsonNode(first).path("translation_id").asText();
            mockMvc.perform(post(example.path()).contentType(MediaType.APPLICATION_JSON)
                    .content(body(example.source().version())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.translation_id").value(id));
            assertThat(store.findById(id).orElseThrow().translatedJson()).contains(example.expected());
            verify(aiClient, times(1)).translate(example.source().texts(), SupportedLanguage.ENGLISH);
        }
        when(aiClient.detectLanguage(examples.get(0).source().texts())).thenReturn("ko-KR");
        for (int repeat = 0; repeat < 2; repeat++) {
            mockMvc.perform(post("/translations/source-language").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"resource_type\":\"ARTICLE_CONTENT\",\"resource_id\":\"18\",\"target_language\":\"en-US\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.source_language").value("ko-KR"))
                .andExpect(jsonPath("$.data.translation_required").value(true));
        }
        verify(aiClient, times(1)).detectLanguage(examples.get(0).source().texts());
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

    @Test
    void 실패_POST는_오류로_반환하고_결과_GET과_후속_POST는_저장된_대기시간을_존중한다() throws Exception {
        var resource = TranslationResource.article(TranslationType.LOST_ITEM, 17);
        var source = TranslationSource.of(resource, TranslationSourceFields.lostItem("학생회관", null),
            Map.of("found_place", "학생회관"), null);
        when(sourceReader.load(resource, null)).thenReturn(source);
        var upstreamStatus = new AtomicInteger(401);
        var calls = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            calls.incrementAndGet();
            try {
                exchange.getResponseHeaders().set("Retry-After", "7200");
                exchange.sendResponseHeaders(upstreamStatus.get(), -1);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            var upstage = new UpstageProperties();
            upstage.setApiKey("translation-test-key");
            upstage.setApiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            var client = new UpstageTranslationClient(objectMapper, upstage, properties);
            when(aiClient.translate(source.texts(), SupportedLanguage.ENGLISH))
                .thenAnswer(invocation -> client.translate(source.texts(), SupportedLanguage.ENGLISH));
            var rejected = mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.reason_code").value("CONFIGURATION_ERROR"))
                .andExpect(jsonPath("$.retryable").value(false)).andExpect(header().doesNotExist(HttpHeaders.RETRY_AFTER))
                .andReturn();
            assertThat(calls.get()).isEqualTo(1); // 인증 오류는 재시도하지 않는다.
            String id = JsonAssertions.convertJsonNode(rejected).path("translation_id").asText();
            mockMvc.perform(get("/translations/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.retryable").value(false)).andExpect(header().doesNotExist(HttpHeaders.RETRY_AFTER));

            // 운영자가 설정을 수정한 뒤 다시 요청한 상황. 외부 대기 지시는 25초보다 길다.
            jdbc.update("UPDATE content_translations SET next_attempt_at = ? WHERE id = ?", LocalDateTime.now(clock), id);
            upstreamStatus.set(429);
            long started = System.nanoTime();
            var limited = mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.reason_code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.retryable").value(true)).andExpect(jsonPath("$.translation_id").value(id)).andReturn();
            assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(5));
            assertThat(Long.parseLong(limited.getResponse().getHeader(HttpHeaders.RETRY_AFTER))).isGreaterThanOrEqualTo(7200);
            assertThat(store.findById(id).orElseThrow().nextAttemptAt()).isEqualTo(LocalDateTime.now(clock).plusHours(2));
            mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.translation_id").value(id));
            mockMvc.perform(get("/translations/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.reason_code").value("RATE_LIMITED"));
            assertThat(calls.get()).isEqualTo(2); // 기다릴 수 없거나 대기 중인 요청은 Solar를 다시 호출하지 않는다.

            jdbc.update("UPDATE content_translations SET next_attempt_at = ? WHERE id = ?", LocalDateTime.now(clock), id);
            doThrow(new TranslationFailureException(TranslationReason.TIMEOUT, null))
                .when(aiClient).translate(source.texts(), SupportedLanguage.ENGLISH);
            mockMvc.perform(post(LOST).contentType(MediaType.APPLICATION_JSON).content(body(source.version())))
                .andExpect(status().isGatewayTimeout()).andExpect(jsonPath("$.reason_code").value("TIMEOUT"))
                .andExpect(jsonPath("$.retryable").value(true));
        } finally {
            server.stop(0);
        }
    }

    private String body(String version) {
        return "{\"target_language\":\"en-us\",\"expected_source_version\":\"" + version + "\"}";
    }
}
