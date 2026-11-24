package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.translation.model.TranslationReason;
import in.koreatech.koin.domain.translation.service.TranslationFailureException;
import in.koreatech.koin.domain.translation.service.TranslationProperties;

class UpstageTranslationClientTest {
    @Test
    void 다른_문자_혼입은_거절하고_원문의_고유명사와_보호값은_보존한다() throws Exception {
        var mapper = new ObjectMapper();
        var output = new AtomicReference<>(Map.of("content", ""));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = mapper.writeValueAsBytes(Map.of("choices", List.of(Map.of(
                "finish_reason", "stop", "message", Map.of("content", mapper.writeValueAsString(output.get()))))));
            try {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            var upstage = new UpstageProperties();
            upstage.setApiKey("translation-test-key");
            upstage.setApiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            var properties = new TranslationProperties();
            properties.setMaxRetries(0);
            var client = new UpstageTranslationClient(mapper, upstage, properties);
            var fields = Map.of("content", "山田さん KOIN에서 만나요. 15:30, 12000원. https://example.com/menu");
            var sentences = Map.of(
                SupportedLanguage.KOREAN, "山田さん KOIN에서 만나요.",
                SupportedLanguage.ENGLISH, "Meet 山田さん at KOIN.",
                SupportedLanguage.SIMPLIFIED_CHINESE, "在 KOIN 与山田さん见面。",
                SupportedLanguage.MONGOLIAN, "KOIN дээр 山田さん-тай уулзъя.",
                SupportedLanguage.UZBEK, "KOIN da 山田さん bilan uchrashamiz.",
                SupportedLanguage.BURMESE, "KOIN တွင် 山田さん နှင့် တွေ့ကြစို့။",
                SupportedLanguage.JAPANESE, "KOINで山田さんに会いましょう。"
            );
            for (var entry : sentences.entrySet()) {
                String valid = entry.getValue() + " __KOIN_0__, __KOIN_1__.";
                output.set(Map.of("content", valid));
                assertThat(client.translate(fields, entry.getKey()).get("content"))
                    .as(entry.getKey().getCode()).contains("山田さん", "KOIN", "15:30", "12000원", "https://example.com/menu")
                    .doesNotContain("__KOIN_");

                // 미얀마어 실응답의 'တွေ့ကြましょう'처럼 목표 언어 안에 붙은 다른 언어도 차단한다.
                String foreign = entry.getKey() == SupportedLanguage.JAPANESE ? "မင်္ဂလာပါ" : "ましょう";
                output.set(Map.of("content", valid + entry.getValue() + foreign));
                var failure = catchThrowableOfType(() -> client.translate(fields, entry.getKey()),
                    TranslationFailureException.class);
                assertThat(failure).as(entry.getKey().getCode()).isNotNull();
                assertThat(failure.getReason()).isEqualTo(TranslationReason.INVALID_TRANSLATION);
            }
            // 앞선 실호출에서 관찰한 응답을 고정해 비결정적인 Solar 재현 없이도 회귀를 잡는다.
            output.set(Map.of("content", "မင်္ဂလာပါ။ __KOIN_0__ တွင် တွေ့ကြましょう။ "
                + "ကုန်ကျစရိတ်မှာ __KOIN_1__ ဖြစ်ပါသည်။"));
            var mixed = catchThrowableOfType(() -> client.translate(Map.of("content",
                "안녕하세요. 15:30에 만나요. 비용은 12000원입니다. https://example.com/menu"),
                SupportedLanguage.BURMESE), TranslationFailureException.class);
            assertThat(mixed).isNotNull();
            assertThat(mixed.getReason()).isEqualTo(TranslationReason.INVALID_TRANSLATION);

            // 원문의 다른 필드에 있던 고유명사를 빌려와 검사를 통과할 수 없다.
            output.set(Map.of("title", "山田さん", "content", "山田さん မင်္ဂလာပါ။"));
            var failure = catchThrowableOfType(() -> client.translate(
                Map.of("title", "山田さん", "content", "안녕하세요."), SupportedLanguage.BURMESE),
                TranslationFailureException.class);
            assertThat(failure).isNotNull();
            assertThat(failure.getReason()).isEqualTo(TranslationReason.INVALID_TRANSLATION);

            // 판별도 같은 제한시간/재시도 HTTP 경로를 사용하되 번역 결과를 생성하지 않는다.
            output.set(Map.of("source_language", "uz-Latn"));
            assertThat(client.detectLanguage(Map.of("content", "Ertaga uchrashamiz."))).isEqualTo("uz-Latn");
            output.set(Map.of("source_language", "not-a-language"));
            var invalidLanguage = catchThrowableOfType(() -> client.detectLanguage(Map.of("content", "Hello")),
                TranslationFailureException.class);
            assertThat(invalidLanguage.getReason()).isEqualTo(TranslationReason.INVALID_TRANSLATION);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void 필드가_빠진_번역은_거절하고_로그에_원문과_응답을_남기지_않는다() throws Exception {
        var mapper = new ObjectMapper();
        var calls = new AtomicInteger();
        var fields = Map.of("title", "private-source-title", "content", "private-source-content");
        String incomplete = mapper.writeValueAsString(Map.of("title", "private-response-title"));
        byte[] body = mapper.writeValueAsBytes(Map.of("choices", List.of(Map.of(
            "finish_reason", "stop", "message", Map.of("content", incomplete)))));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            calls.incrementAndGet();
            try {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } finally {
                exchange.close();
            }
        });
        var logger = (Logger)LoggerFactory.getLogger(UpstageTranslationClient.class);
        Level previousLevel = logger.getLevel();
        var logs = new ListAppender<ILoggingEvent>();
        logs.start();
        logger.setLevel(Level.WARN);
        logger.addAppender(logs);
        server.start();
        try {
            var upstage = new UpstageProperties();
            upstage.setApiKey("private-api-key");
            upstage.setApiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            var properties = new TranslationProperties();
            properties.setMaxRetries(2);
            var client = new UpstageTranslationClient(mapper, upstage, properties);

            var failure = catchThrowableOfType(() -> client.translate(fields, SupportedLanguage.ENGLISH),
                TranslationFailureException.class);

            assertThat(calls.get()).isEqualTo(3); // 최초 호출 + 설정한 추가 재시도 2회
            assertThat(failure.getReason()).isEqualTo(TranslationReason.MISSING_TRANSLATION);
            assertThat(failure.getMessage()).isEqualTo("MISSING_TRANSLATION");
            assertThat(failure.getCause()).isNull();
            assertThat(logs.list).isNotEmpty().allSatisfy(event -> {
                assertThat(event.getThrowableProxy()).isNull();
                assertThat(event.getFormattedMessage()).contains("reason=MISSING_TRANSLATION")
                    .doesNotContain("private-source", "private-response", "private-api-key");
            });
        } finally {
            server.stop(0);
            logger.detachAppender(logs);
            logs.stop();
            logger.setLevel(previousLevel);
        }
    }

    @Test
    void 일시_장애와_잘못된_번역만_재시도하고_정상_결과를_반환한다() throws Exception {
        var calls = new AtomicInteger();
        var mapper = new ObjectMapper();
        byte[] valid = mapper.writeValueAsBytes(Map.of("choices", List.of(Map.of(
            "finish_reason", "stop", "message", Map.of("content", "{\"content\":\"Hello\"}")))));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            int attempt = calls.incrementAndGet();
            byte[] response = attempt == 3 ? valid : "{}".getBytes();
            try {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Retry-After", "0");
                exchange.sendResponseHeaders(attempt == 1 ? 429 : 200, response.length);
                exchange.getResponseBody().write(response);
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            var properties = new TranslationProperties();
            properties.setMaxRetries(2);
            var client = client(server, properties);
            assertThat(client.translate(Map.of("content", "안녕하세요"), SupportedLanguage.ENGLISH))
                .containsEntry("content", "Hello");
            assertThat(calls.get()).isEqualTo(3);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void 재시도_대기와_호출은_전체_제한_시간을_공유한다() throws Exception {
        var calls = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            int attempt = calls.incrementAndGet();
            try {
                Thread.sleep(attempt == 1 ? 1000 : 5000);
                exchange.sendResponseHeaders(503, -1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        try {
            var properties = new TranslationProperties();
            properties.setRequestTimeoutSeconds(2);
            properties.setMaxRetries(3);
            var client = client(server, properties);
            long started = System.nanoTime();
            var failure = catchThrowableOfType(() -> client.translate(Map.of("content", "안녕하세요"),
                SupportedLanguage.ENGLISH), TranslationFailureException.class);
            assertThat(failure.getReason()).isEqualTo(TranslationReason.TIMEOUT);
            assertThat(calls.get()).isEqualTo(2);
            assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofMillis(2900));
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private UpstageTranslationClient client(HttpServer server, TranslationProperties properties) {
        var upstage = new UpstageProperties();
        upstage.setApiKey("translation-test-key");
        upstage.setApiBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
        return new UpstageTranslationClient(new ObjectMapper(), upstage, properties);
    }
}
