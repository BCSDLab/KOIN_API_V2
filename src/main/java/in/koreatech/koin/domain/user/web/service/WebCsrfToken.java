package in.koreatech.koin.domain.user.web.service;

import java.time.Instant;

public record WebCsrfToken(String value, Instant expiresAt, boolean autoLogin) {

    @Override
    public String toString() {
        return "WebCsrfToken[REDACTED]";
    }
}
