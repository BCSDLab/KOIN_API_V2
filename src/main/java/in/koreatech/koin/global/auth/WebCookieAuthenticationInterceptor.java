package in.koreatech.koin.global.auth;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import in.koreatech.koin.domain.user.web.controller.WebAuthController;
import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebCookieAuthenticationInterceptor implements HandlerInterceptor {

    private final WebAuthService webAuthService;
    private final WebAuthCookieManager cookieManager;
    private final WebAuthRequestValidator requestValidator;
    private final AuthContext authContext;
    private final UserIdContext userIdContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod) || "OPTIONS".equals(request.getMethod())) {
            return true;
        }
        if (WebAuthController.class.isAssignableFrom(handlerMethod.getBeanType())) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            requestValidator.requireTrustedOrigin(request);
            return true;
        }
        // 명시적인 Authorization 헤더가 있으면 기존 인증만 사용하고 쿠키로 대체하지 않는다.
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            return true;
        }
        boolean usesAuthentication = Arrays.stream(handlerMethod.getMethodParameters())
            .anyMatch(parameter -> parameter.hasParameterAnnotation(Auth.class)
                || parameter.hasParameterAnnotation(UserId.class));
        if (!usesAuthentication) {
            return true;
        }
        String accessToken = cookieManager.getAccessToken(request);
        if (accessToken != null) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
            WebAuthSession session = webAuthService.authenticate(accessToken);
            requestValidator.validate(request, session);
            authContext.setUserId(session.userId());
            userIdContext.setUserId(session.userId());
        }
        return true;
    }
}
