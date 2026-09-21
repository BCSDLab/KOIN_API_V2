package in.koreatech.koin.global.auth;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    private static final String WEB_SESSION_ID = "web_session_id";

    private final String secretKey;
    private final Long expirationTime;

    public JwtProvider(
        @Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.access-token.expiration-time}") Long expirationTime
    ) {
        this.secretKey = secretKey;
        this.expirationTime = expirationTime;
    }

    public String createToken(User user) {
        return createToken(user, null, Instant.now().plusMillis(expirationTime));
    }

    public String createWebToken(User user, String sessionId, Instant expiresAt) {
        return createToken(user, sessionId, expiresAt);
    }

    private String createToken(User user, String sessionId, Instant expiresAt) {
        if (user == null) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_USER, "user: " + null);
        }
        Key key = getSecretKey();
        return Jwts.builder()
            .signWith(key)
            .header()
            .add("typ", "JWT")
            .add("alg", key.getAlgorithm())
            .and()
            .claim("id", user.getId())
            .claim(WEB_SESSION_ID, sessionId)
            .expiration(Date.from(expiresAt))
            .compact();
    }

    /**
     * 임시 회원가입 토큰 생성
     */
    public String createTemporaryToken() {
        Key key = getSecretKey();
        return Jwts.builder()
            .signWith(key)
            .header()
            .add("typ", "JWT")
            .add("alg", key.getAlgorithm())
            .and()
            .claim("id", UserType.ANONYMOUS_ID)
            .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
            .compact();
    }

    public Integer getUserId(String token) {
        Claims claims = getClaims(token);
        if (claims.containsKey(WEB_SESSION_ID)) {
            throw AuthenticationException.withDetail("웹 토큰은 쿠키 인증에서만 사용할 수 있습니다.");
        }
        return getUserId(claims);
    }

    public WebTokenClaims getWebTokenClaims(String token) {
        Claims claims = getClaims(token);
        Object sessionId = claims.get(WEB_SESSION_ID);
        if (!(sessionId instanceof String value) || value.isBlank()) {
            throw AuthenticationException.withDetail("웹 로그인 세션 정보가 없습니다.");
        }
        return new WebTokenClaims(getUserId(claims), value);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw AuthenticationException.withDetail("토큰 검증에 실패했습니다.");
        }
    }

    private Integer getUserId(Claims claims) {
        try {
            return Integer.valueOf(String.valueOf(claims.get("id")));
        } catch (NumberFormatException e) {
            throw AuthenticationException.withDetail("토큰의 사용자 정보가 올바르지 않습니다.");
        }
    }

    public record WebTokenClaims(Integer userId, String sessionId) {

    }

    private SecretKey getSecretKey() {
        String encoded = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Keys.hmacShaKeyFor(encoded.getBytes());
    }
}
