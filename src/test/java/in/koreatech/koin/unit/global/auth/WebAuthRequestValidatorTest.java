package in.koreatech.koin.unit.global.auth;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;

import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.global.auth.WebAuthRequestValidator;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.CorsProperties;
import in.koreatech.koin.global.exception.CustomException;

class WebAuthRequestValidatorTest {

    private static final String ORIGIN = "https://koreatech.in";
    private final WebAuthRequestValidator validator = new WebAuthRequestValidator(new CorsProperties(List.of(ORIGIN)));
    private final WebAuthSession session = new WebAuthSession(
        "session", 1, "refresh-hash", "csrf-secret", "credential-hash", Instant.now().plusSeconds(3600), true
    );

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"null", "*", "https://evil.example", "https://koreatech.in.evil.example", "https://stage.koreatech.in"})
    void 허용하지_않은_origin은_거부한다(String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/password");
        if (origin != null) {
            request.addHeader("Origin", origin);
        }
        request.addHeader("X-CSRF-Token", session.csrfToken());

        assertThatThrownBy(() -> validator.validate(request, session))
            .isInstanceOf(CustomException.class)
            .hasMessage(ApiResponseCode.FORBIDDEN_WEB_ORIGIN.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE", "TRACE"})
    void 상태_변경_요청은_세션의_csrf_토큰도_필요하다(String method) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/users/password");
        request.addHeader("Origin", ORIGIN);
        request.addHeader("X-CSRF-Token", "another-session-token");

        assertThatThrownBy(() -> validator.validate(request, session))
            .isInstanceOf(CustomException.class)
            .hasMessage(ApiResponseCode.INVALID_CSRF_TOKEN.getMessage());
    }

    @Test
    void origin과_csrf_토큰이_일치하면_허용한다() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/password");
        request.addHeader("Origin", ORIGIN);
        request.addHeader("X-CSRF-Token", session.csrfToken());

        assertThatCode(() -> validator.validate(request, session)).doesNotThrowAnyException();
    }

    @Test
    void origin이_없는_동일_출처_요청은_referer로_확인한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", ORIGIN + "/login?next=/");

        assertThatCode(() -> validator.requireTrustedOrigin(request)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://koreatech.in@evil.example/", "https://evil.example/koreatech.in", "not a uri"})
    void 위장한_referer는_거부한다(String referer) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", referer);

        assertThatThrownBy(() -> validator.requireTrustedOrigin(request)).isInstanceOf(CustomException.class);
    }

    @Test
    void 잘못된_origin을_정상_referer로_우회할_수_없다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "null");
        request.addHeader("Referer", ORIGIN + "/");

        assertThatThrownBy(() -> validator.requireTrustedOrigin(request)).isInstanceOf(CustomException.class);
    }

    @Test
    void 조회는_csrf_헤더_없이_허용한다() {
        assertThatCode(() -> validator.validate(new MockHttpServletRequest("GET", "/user/auth"), session))
            .doesNotThrowAnyException();
    }
}
