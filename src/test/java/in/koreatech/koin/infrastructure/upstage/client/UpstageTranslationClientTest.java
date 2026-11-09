package in.koreatech.koin.infrastructure.upstage.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

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
    void 필드가_빠진_번역은_거절하고_로그에_원문과_응답을_남기지_않는다() throws Exception {
        var mapper = new ObjectMapper();
        var fields = Map.of("title", "private-source-title", "content", "private-source-content");
        String incomplete = mapper.writeValueAsString(Map.of("title", "private-response-title"));
        byte[] body = mapper.writeValueAsBytes(Map.of("choices", List.of(Map.of(
            "finish_reason", "stop", "message", Map.of("content", incomplete)))));
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
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
            var client = new UpstageTranslationClient(mapper, upstage, new TranslationProperties());

            var failure = catchThrowableOfType(() -> client.translate(fields, SupportedLanguage.ENGLISH),
                TranslationFailureException.class);

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
}
