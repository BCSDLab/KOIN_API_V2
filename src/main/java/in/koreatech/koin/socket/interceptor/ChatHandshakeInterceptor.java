package in.koreatech.koin.socket.interceptor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.auth.WebAuthRequestValidator;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    public static final String AUTH_CONTEXT = ChatHandshakeInterceptor.class.getName() + ".cookie";
    private final WebAuthCookieManager cookieManager;
    private final WebAuthRequestValidator requestValidator;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler handler, Map<String, Object> attributes) {
        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                var servlet = servletRequest.getServletRequest();
                String token = cookieManager.getAccessToken(servlet);
                if (token != null) {
                    boolean trustedOrigin = true;
                    try {
                        requestValidator.requireTrustedOrigin(servlet);
                    } catch (AuthenticationException exception) {
                        trustedOrigin = false;
                    } catch (CustomException exception) {
                        if (exception.getErrorCode().getHttpStatus() != HttpStatus.FORBIDDEN) {
                            throw exception;
                        }
                        trustedOrigin = false;
                    }
                    // CONNECT의 명시적 Authorization 우선순위에 따라 그 시점에 인증한다.
                    attributes.put(AUTH_CONTEXT, new CookieContext(token, trustedOrigin));
                }
            }
            return true;
        } catch (AuthenticationException exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        } catch (CustomException exception) {
            HttpStatus status = exception.getErrorCode().getHttpStatus();
            if (status != HttpStatus.UNAUTHORIZED && status != HttpStatus.FORBIDDEN) {
                throw exception;
            }
            response.setStatusCode(status);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler handler, Exception exception) {
    }

    public record CookieContext(String accessToken, boolean trustedOrigin) {
        @Override
        public String toString() {
            return "CookieContext[redacted]";
        }
    }
}
