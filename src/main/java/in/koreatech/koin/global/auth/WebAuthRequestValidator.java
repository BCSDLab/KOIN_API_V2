package in.koreatech.koin.global.auth;

import java.net.URI;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.CorsProperties;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebAuthRequestValidator {

    public static final String CSRF_HEADER = "X-CSRF-Token";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final CorsProperties corsProperties;

    public void validate(HttpServletRequest request, WebAuthSession session) {
        // 기존 조회 API 중 읽음 상태를 갱신하는 요청도 있으므로 GET도 출처를 확인한다.
        requireTrustedOrigin(request);
        if (!SAFE_METHODS.contains(request.getMethod())) {
            session.requireCsrfToken(request.getHeader(CSRF_HEADER));
        }
    }

    public void requireTrustedOrigin(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null) {
            origin = originFromReferer(request.getHeader(HttpHeaders.REFERER));
        }
        if (origin == null || "null".equals(origin) || "*".equals(origin)
            || corsProperties.allowedOrigins() == null || !corsProperties.allowedOrigins().contains(origin)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_WEB_ORIGIN);
        }
    }

    private String originFromReferer(String referer) {
        if (referer == null) {
            return null;
        }
        try {
            URI uri = URI.create(referer);
            if (uri.getHost() == null || uri.getUserInfo() != null
                || !("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))) {
                return null;
            }
            return uri.getScheme() + "://" + uri.getRawAuthority();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
