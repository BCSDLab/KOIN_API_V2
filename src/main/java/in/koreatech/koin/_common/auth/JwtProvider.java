package in.koreatech.koin._common.auth;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin._common.auth.exception.AuthenticationException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

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
        if (user == null) {
            throw UserNotFoundException.withDetail("user: " + null);
        }
        Key key = getSecretKey();
        return Jwts.builder()
            .signWith(key)
            .header()
            .add("typ", "JWT")
            .add("alg", key.getAlgorithm())
            .and()
            .claim("id", user.getId())
            .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
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

    /**
     * SMS 인증된 휴대폰 번호로 임시 토큰을 생성합니다.
     * 생성된 토큰은 @SmsAuthed 어노테이션으로 검증할 수 있습니다.
     */
    public String createTemporaryTokenWithPhone(String phoneNumber) {
        Key key = getSecretKey();
        return Jwts.builder()
            .signWith(key)
            .header()
            .add("typ", "JWT")
            .add("alg", key.getAlgorithm())
            .and()
            .claim("id", UserType.ANONYMOUS_ID)
            .claim("phone", phoneNumber)
            .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
            .compact();
    }

    public Integer getUserId(String token) {
        try {
            String userId = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id")
                .toString();
            return Integer.parseInt(userId);
        } catch (JwtException e) {
            throw AuthenticationException.withDetail("token: " + token);
        }
    }

    /**
     * JWT 토큰에서 휴대폰 번호를 추출합니다.
     * @param token JWT 토큰
     * @return 휴대폰 번호 (없는 경우 null)
     */
    public String getPhoneNumber(String token) {
        try {
            return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("phone", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    private SecretKey getSecretKey() {
        String encoded = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Keys.hmacShaKeyFor(encoded.getBytes());
    }
}
