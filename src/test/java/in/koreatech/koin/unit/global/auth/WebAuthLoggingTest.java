package in.koreatech.koin.unit.global.auth;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import in.koreatech.koin.domain.user.web.dto.WebLoginRequest;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.GlobalExceptionHandler;

class WebAuthLoggingTest {

    @Test
    void 잘못된_JSON의_파싱_오류에도_로그인_비밀값을_기록하지_않는다() throws Exception {
        Logger logger = (Logger)LoggerFactory.getLogger(GlobalExceptionHandler.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v2/web/auth/login");
            request.setContentType("application/json");
            request.setContent("{\"login_id\":\"test\",\"login_pw\":malformedPasswordMarker}".getBytes(UTF_8));
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            Throwable error = catchThrowable(() -> converter.read(WebLoginRequest.class, new ServletServerHttpRequest(request)));
            assertThat(error).isInstanceOf(HttpMessageNotReadableException.class);

            new GlobalExceptionHandler().handleException((Exception)error,
                new ServletWebRequest(request, new MockHttpServletResponse()));

            String messages = appender.list.stream().map(ILoggingEvent::getFormattedMessage).collect(joining("\n"));
            assertThat(messages).isNotBlank().doesNotContain("malformedPasswordMarker");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

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
