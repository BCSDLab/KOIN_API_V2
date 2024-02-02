package in.koreatech.koin.global.auth;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.auth.exception.AuthException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token.expiration-time}")
    private Long expirationTime;

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
            .expiration(new Date(Instant.now().toEpochMilli() + expirationTime))
            .compact();
    }

    public Long getUserId(String token) {
        try {
            String userId = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id")
                .toString();
            return Long.parseLong(userId);

        } catch (JwtException e) {
            throw AuthException.withDetail("token: " + token);
        }
    }

    private SecretKey getSecretKey() {
        String encoded = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Keys.hmacShaKeyFor(encoded.getBytes());
    }

}
