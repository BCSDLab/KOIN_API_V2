package in.koreatech.koin.global.config;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "auth.web")
public record WebAuthProperties(
    @DefaultValue("15m") Duration accessTokenTtl,
    @DefaultValue("90d") Duration refreshTokenTtl,
    @DefaultValue("true") boolean secure,
    @DefaultValue("Lax") String sameSite,
    @DefaultValue("") String sharedCookieDomain,
    @DefaultValue("koin-web") String cookieNamePrefix
) {

    public WebAuthProperties {
        if (accessTokenTtl.toSeconds() < 1 || refreshTokenTtl.compareTo(accessTokenTtl) < 0) {
            throw new IllegalArgumentException("웹 토큰 만료 시간은 양수이며 refresh가 access 이상이어야 합니다.");
        }
        if (!Set.of("Lax", "Strict", "None").contains(sameSite) || ("None".equals(sameSite) && !secure)) {
            throw new IllegalArgumentException("올바른 SameSite 설정이 필요하며 None은 Secure 쿠키만 허용합니다.");
        }
        sharedCookieDomain = sharedCookieDomain == null || sharedCookieDomain.isBlank() ? null
            : sharedCookieDomain.toLowerCase(Locale.ROOT).replaceFirst("^\\.", "");
        if (sharedCookieDomain != null && (sharedCookieDomain.length() > 253
            || !sharedCookieDomain.matches("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)+"))) {
            throw new IllegalArgumentException("쿠키 공유 도메인은 포트나 경로가 없는 도메인이어야 합니다.");
        }
        if (cookieNamePrefix == null || !cookieNamePrefix.matches("[a-zA-Z0-9][a-zA-Z0-9_-]{0,63}")) {
            throw new IllegalArgumentException("올바른 웹 쿠키 이름 접두어가 필요합니다.");
        }
    }

    public String accessCookieName() {
        return sharedCookiePrefix() + cookieNamePrefix + "-access";
    }

    public String refreshCookieName() {
        return (secure ? "__Secure-" : "") + cookieNamePrefix + "-refresh";
    }

    public String csrfCookieName() {
        return sharedCookiePrefix() + cookieNamePrefix + "-csrf";
    }

    private String sharedCookiePrefix() {
        return secure ? (sharedCookieDomain == null ? "__Host-" : "__Secure-") : "";
    }
}
