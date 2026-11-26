package in.koreatech.koin.unit.socket;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.auth.WebAuthRequestValidator;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.socket.interceptor.ChatHandshakeInterceptor;

class ChatHandshakeInterceptorTest {
    private final WebAuthCookieManager cookies = mock(WebAuthCookieManager.class);
    private final WebAuthRequestValidator origins = mock(WebAuthRequestValidator.class);
    private final ChatHandshakeInterceptor interceptor = new ChatHandshakeInterceptor(cookies, origins);

    @Test
    void cookieAbsenceAllowsLaterNativeAuthorization() {
        var request = new MockHttpServletRequest();
        var attributes = new HashMap<String, Object>();
        assertThat(interceptor.beforeHandshake(new ServletServerHttpRequest(request), null, null, attributes)).isTrue();
        assertThat(attributes).isEmpty();
        verifyNoInteractions(origins);
    }

    @Test
    void cookieIsCapturedAfterTrustedOriginValidationForAuthenticationAtConnect() {
        var request = new MockHttpServletRequest();
        var attributes = new HashMap<String, Object>();
        when(cookies.getAccessToken(request)).thenReturn("cookie-token");

        assertThat(interceptor.beforeHandshake(new ServletServerHttpRequest(request), null, null, attributes))
            .isTrue();

        verify(origins).requireTrustedOrigin(request);
        assertThat(attributes.get(ChatHandshakeInterceptor.AUTH_CONTEXT))
            .isEqualTo(new ChatHandshakeInterceptor.CookieContext("cookie-token", true));
        assertThat(attributes.get(ChatHandshakeInterceptor.AUTH_CONTEXT).toString()).doesNotContain("cookie-token");
    }

    @Test
    void untrustedOriginIsCapturedForConnectToChooseBearerOrCookie() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        when(cookies.getAccessToken(request)).thenReturn("cookie-token");
        doThrow(CustomException.of(ApiResponseCode.FORBIDDEN_WEB_ORIGIN))
            .when(origins).requireTrustedOrigin(request);
        var attributes = new HashMap<String, Object>();

        assertThat(interceptor.beforeHandshake(new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(response), null, attributes)).isTrue();

        assertThat(attributes.get(ChatHandshakeInterceptor.AUTH_CONTEXT))
            .isEqualTo(new ChatHandshakeInterceptor.CookieContext("cookie-token", false));
    }

    @Test
    void duplicateCookieReturnsUnauthorizedBeforeOriginValidation() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        when(cookies.getAccessToken(request)).thenThrow(AuthenticationException.withDetail("duplicate"));

        assertThat(interceptor.beforeHandshake(new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(response), null, new HashMap<>())).isFalse();

        assertThat(response.getStatus()).isEqualTo(401);
        verifyNoInteractions(origins);
    }

    @Test
    void recognizedCustomAuthenticationErrorPreservesUnauthorizedStatus() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        when(cookies.getAccessToken(request)).thenThrow(CustomException.of(ApiResponseCode.UNAUTHORIZED_USER));

        assertThat(interceptor.beforeHandshake(new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(response), null, new HashMap<>())).isFalse();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void unexpectedInfrastructureFailuresAreNotConvertedToAuthenticationFailures() {
        var request = new MockHttpServletRequest();
        var infrastructureFailure = new IllegalStateException("redis unavailable");
        when(cookies.getAccessToken(request)).thenThrow(infrastructureFailure);

        assertThatThrownBy(() -> interceptor.beforeHandshake(new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(new MockHttpServletResponse()), null, new HashMap<>()))
            .isSameAs(infrastructureFailure);
    }

    @Test
    void customServerErrorsAreNotConvertedToAuthenticationFailures() {
        var request = new MockHttpServletRequest();
        var infrastructureFailure = CustomException.of(ApiResponseCode.INTERNAL_SERVER_ERROR);
        when(cookies.getAccessToken(request)).thenThrow(infrastructureFailure);

        assertThatThrownBy(() -> interceptor.beforeHandshake(new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(new MockHttpServletResponse()), null, new HashMap<>()))
            .isSameAs(infrastructureFailure);
    }
}
