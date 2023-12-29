package in.koreatech.koin.domain.auth;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token.expiration-time}")
    private Long expirationTime;

    public String createToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
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

    private SecretKey getSecretKey() {
        String encoded = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Keys.hmacShaKeyFor(encoded.getBytes());
    }
}
