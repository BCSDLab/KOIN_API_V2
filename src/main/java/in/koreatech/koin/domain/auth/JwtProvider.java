package in.koreatech.koin.domain.auth;

import in.koreatech.koin.domain.auth.exception.AuthException;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String BEARER_PREFIX = "BEARER ";

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token.expiration-time}")
    private Long expirationTime;

    public String createToken(User user) {
        if (user == null) {
            throw new UserNotFoundException("존재하지 않는 사용자입니다. user: " + user);
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

    public Long getUserId(String requestToken) {
        if (requestToken == null || !requestToken.toUpperCase().startsWith(BEARER_PREFIX)) {
            throw AuthException.withDetail("token: " + requestToken);
        }
        String token = requestToken.substring(BEARER_PREFIX.length());

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
