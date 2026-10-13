package in.koreatech.koin.global.auth;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import in.koreatech.koin.global.auth.exception.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExtractAuthenticationInterceptor implements HandlerInterceptor {

    private static final String BEARER_TYPE = "Bearer ";
    private static final int BEARER_TYPE_LEN = 7;

    private final JwtProvider jwtProvider;
    private final AuthContext authContext;
    private final UserIdContext userIdContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractAccessToken(request);
        if (token == null) {
            return true;
        }
        try {
            Integer userId = jwtProvider.getUserId(token);
            authContext.setUserId(userId);
            userIdContext.setUserId(userId);
        } catch (AuthenticationException e) {
            if (!canIgnoreInvalidToken(handler)) {
                throw e;
            }
        }
        return true;
    }

    static boolean canIgnoreInvalidToken(Object handler) {
        return handler instanceof HandlerMethod handlerMethod
            && Arrays.stream(handlerMethod.getMethodParameters())
                .noneMatch(parameter -> parameter.hasParameterAnnotation(Auth.class));
    }

    public static String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            return bearerToken.substring(BEARER_TYPE_LEN);
        }
        return null;
    }
}
