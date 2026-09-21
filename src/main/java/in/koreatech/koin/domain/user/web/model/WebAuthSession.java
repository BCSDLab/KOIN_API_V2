package in.koreatech.koin.domain.user.web.model;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.time.Instant;

import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public record WebAuthSession(
    String id,
    Integer userId,
    String refreshTokenHash,
    String csrfToken,
    String credentialHash,
    Instant expiresAt,
    boolean autoLogin
) {

    public void requireRefreshToken(WebRefreshToken token) {
        if (!matches(refreshTokenHash, token.hash())) {
            throw AuthenticationException.withDetail("웹 재발급 토큰이 일치하지 않습니다.");
        }
    }

    public void requireCsrfToken(String token) {
        if (!matches(csrfToken, token)) {
            throw CustomException.of(ApiResponseCode.INVALID_CSRF_TOKEN);
        }
    }

    public void requireNotExpired() {
        if (!expiresAt.isAfter(Instant.now())) {
            throw AuthenticationException.withDetail("웹 로그인 유지 기간이 만료되었습니다.");
        }
    }

    public WebAuthSession rotate(WebRefreshToken token) {
        return new WebAuthSession(id, userId, token.hash(), csrfToken, credentialHash, expiresAt, autoLogin);
    }

    private boolean matches(String expected, String actual) {
        return actual != null && MessageDigest.isEqual(expected.getBytes(UTF_8), actual.getBytes(UTF_8));
    }

    @Override
    public String toString() {
        return "WebAuthSession[REDACTED]";
    }
}
