package in.koreatech.koin.unit.global.auth;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.GlobalExceptionHandler;

class WebAuthLoggingTest {

    @Test
    void 인증_실패_로그에_쿠키와_csrf_토큰과_로그인_본문을_노출하지_않는다() {
        Logger logger = (Logger)LoggerFactory.getLogger(GlobalExceptionHandler.class);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        try {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v2/web/auth/login");
            request.addHeader("AUTHORIZATION", "Bearer access-secret");
            request.addHeader("Cookie", "koin-web-refresh=refresh-secret");
            request.addHeader("X-CSRF-Token", "csrf-secret");
            request.setContent("{\"login_pw\":\"password-secret\"}".getBytes(UTF_8));

            new GlobalExceptionHandler().handleCustomException(new ContentCachingRequestWrapper(request),
                CustomException.of(ApiResponseCode.FORBIDDEN_WEB_ORIGIN));

            String messages = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .collect(joining("\n"));
            assertThat(messages).contains("[REDACTED]")
                .doesNotContain("access-secret", "refresh-secret", "csrf-secret", "password-secret");
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
        }
    }
}
