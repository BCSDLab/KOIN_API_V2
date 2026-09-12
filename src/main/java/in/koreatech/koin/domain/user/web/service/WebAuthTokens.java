package in.koreatech.koin.domain.user.web.service;

import java.time.Instant;

import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;

public record WebAuthTokens(
    String accessToken,
    String refreshToken,
    Instant accessExpiresAt,
    Instant refreshExpiresAt,
    boolean autoLogin,
    WebAuthResponse response
) {

    @Override
    public String toString() {
        return "WebAuthTokens[REDACTED]";
    }
}
