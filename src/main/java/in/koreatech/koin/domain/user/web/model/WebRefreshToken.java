package in.koreatech.koin.domain.user.web.model;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import java.util.regex.Pattern;

import in.koreatech.koin.global.auth.exception.AuthenticationException;

public record WebRefreshToken(String sessionId, String secret) {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.[A-Za-z0-9_-]{43}"
    );

    public static WebRefreshToken create() {
        return new WebRefreshToken(UUID.randomUUID().toString(), createSecret());
    }

    public static WebRefreshToken parse(String value) {
        if (value == null || !TOKEN_PATTERN.matcher(value).matches()) {
            throw AuthenticationException.withDetail("올바르지 않은 웹 재발급 토큰입니다.");
        }
        int separator = value.indexOf('.');
        return new WebRefreshToken(value.substring(0, separator), value.substring(separator + 1));
    }

    public WebRefreshToken rotate() {
        return new WebRefreshToken(sessionId, createSecret());
    }

    public String value() {
        return sessionId + "." + secret;
    }

    public String hash() {
        return hash(value());
    }

    public static String createSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }

    @Override
    public String toString() {
        return "WebRefreshToken[REDACTED]";
    }
}
