package in.koreatech.koin.domain.auth;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class ExtractAuthenticationInterceptor implements HandlerInterceptor {

    private static final String BEARER_TYPE = "Bearer";
    private static final int BEARER_TYPE_LEN = 7;

    private final JwtProvider jwtProvider;
    private final AuthContext authContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Optional.ofNullable(extractAccessToken(request))
            .map(jwtProvider::getUserId)
            .ifPresent(authContext::setUserId);
        return true;
    }

    public static String extractAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            return bearerToken.substring(BEARER_TYPE_LEN);
        }
        return null;
    }
}
