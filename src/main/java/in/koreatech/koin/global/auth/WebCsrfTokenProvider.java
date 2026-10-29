package in.koreatech.koin.global.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

@Component
public class WebCsrfTokenProvider {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}");

    private final SecretKeySpec key;

    public WebCsrfTokenProvider(@Value("${auth.web.csrf-secret-key}") String secretKey) {
        if (!StringUtils.hasText(secretKey) || secretKey.getBytes(UTF_8).length < 32) {
            throw new IllegalArgumentException("웹 CSRF 서명 키는 32바이트 이상이어야 합니다.");
        }
        key = new SecretKeySpec(secretKey.getBytes(UTF_8), ALGORITHM);
    }

    public String createToken(String sessionId) {
        String nonce = WebRefreshToken.createSecret();
        return nonce + "." + signature(sessionId, nonce);
    }

    public void validate(WebAuthSession session, String token) {
        if (token == null || token.length() != 87 || !TOKEN_PATTERN.matcher(token).matches()) {
            throw CustomException.of(ApiResponseCode.INVALID_CSRF_TOKEN);
        }
        String expected = signature(session.id(), token.substring(0, 43));
        if (!MessageDigest.isEqual(expected.getBytes(UTF_8), token.substring(44).getBytes(UTF_8))) {
            throw CustomException.of(ApiResponseCode.INVALID_CSRF_TOKEN);
        }
        session.requireCsrfToken(token);
    }

    private String signature(String sessionId, String nonce) {
        // 세션 ID는 서명 입력으로만 사용하며 클라이언트에 내려주는 CSRF 토큰에는 포함하지 않는다.
        String message = "koin-web-csrf:v1!" + sessionId.length() + "!" + sessionId + "!" + nonce.length() + "!" + nonce;
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(key);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(message.getBytes(UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("웹 CSRF 서명을 생성할 수 없습니다.", e);
        }
    }
}
