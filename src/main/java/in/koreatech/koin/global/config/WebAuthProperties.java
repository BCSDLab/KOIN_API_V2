package in.koreatech.koin.global.config;

import java.time.Duration;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "auth.web")
public record WebAuthProperties(
    @DefaultValue("15m") Duration accessTokenTtl,
    @DefaultValue("90d") Duration refreshTokenTtl,
    @DefaultValue("true") boolean secure,
    @DefaultValue("Lax") String sameSite
) {

    public WebAuthProperties {
        if (accessTokenTtl.toSeconds() < 1 || refreshTokenTtl.compareTo(accessTokenTtl) < 0) {
            throw new IllegalArgumentException("웹 토큰 만료 시간은 양수이며 refresh가 access 이상이어야 합니다.");
        }
        if (!Set.of("Lax", "Strict", "None").contains(sameSite) || ("None".equals(sameSite) && !secure)) {
            throw new IllegalArgumentException("올바른 SameSite 설정이 필요하며 None은 Secure 쿠키만 허용합니다.");
        }
    }

    public String accessCookieName() {
        return secure ? "__Host-koin-web-access" : "koin-web-access";
    }

    public String refreshCookieName() {
        return secure ? "__Secure-koin-web-refresh" : "koin-web-refresh";
    }
}
