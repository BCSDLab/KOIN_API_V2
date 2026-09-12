package in.koreatech.koin.global.auth;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.web.service.WebAuthTokens;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebAuthCookieManager {

    public static final String AUTH_PATH = "/v2/web/auth";

    private final WebAuthProperties properties;

    public String getAccessToken(HttpServletRequest request) {
        return getCookie(request, properties.accessCookieName());
    }

    public String getRefreshToken(HttpServletRequest request) {
        return getCookie(request, properties.refreshCookieName());
    }

    public void write(HttpServletResponse response, WebAuthTokens tokens) {
        addCookie(response, properties.accessCookieName(), tokens.accessToken(), "/",
            maxAge(tokens.accessExpiresAt(), tokens.autoLogin()));
        addCookie(response, properties.refreshCookieName(), tokens.refreshToken(), AUTH_PATH,
            maxAge(tokens.refreshExpiresAt(), tokens.autoLogin()));
    }

    public void clear(HttpServletResponse response) {
        addCookie(response, properties.accessCookieName(), "", "/", 0);
        addCookie(response, properties.refreshCookieName(), "", AUTH_PATH, 0);
    }

    private String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String value = null;
        boolean found = false;
        for (Cookie cookie : cookies) {
            if (!name.equals(cookie.getName())) {
                continue;
            }
            if (found) {
                throw AuthenticationException.withDetail("중복된 웹 인증 쿠키입니다.");
            }
            found = true;
            value = cookie.getValue();
        }
        return value;
    }

    private long maxAge(Instant expiresAt, boolean autoLogin) {
        return autoLogin ? Math.max(0, Duration.between(Instant.now(), expiresAt).toSeconds()) : -1;
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(properties.secure())
            .sameSite(properties.sameSite())
            .path(path)
            .maxAge(maxAge)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
