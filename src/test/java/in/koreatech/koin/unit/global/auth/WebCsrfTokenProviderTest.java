package in.koreatech.koin.unit.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.service.WebCsrfToken;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

class WebCsrfTokenProviderTest {

    private final WebCsrfTokenProvider provider = new WebCsrfTokenProvider("csrf-test-signing-key-at-least-32-bytes");

    @Test
    void 독립적으로_계산한_HMAC_SHA256_검증값과_일치한다() {
        String token = "A".repeat(43) + ".lu4WVMWDGC4r_nrTXPWdHl8KG2IpTPGkqGNIZJ7Flpo";

        assertThatCode(() -> provider.validate(session("session-a", token), token)).doesNotThrowAnyException();
    }

    @Test
    void csrf_전달_객체는_로그에_토큰을_노출하지_않는다() {
        WebCsrfToken token = new WebCsrfToken("sensitive-csrf-value", Instant.now().plusSeconds(3600), true);

        assertThat(token.toString()).isEqualTo("WebCsrfToken[REDACTED]");
    }

    @Test
    void 세션에_연결한_서명을_검증하고_세션_ID는_토큰에_노출하지_않는다() {
        String token = provider.createToken("session-a");

        assertThat(token).matches("[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}").doesNotContain("session-a");
        assertThatCode(() -> provider.validate(session("session-a", token), token)).doesNotThrowAnyException();
        assertThat(provider.createToken("session-a")).isNotEqualTo(token);
    }

    @Test
    void 다른_세션의_유효한_토큰은_저장값까지_같아도_거부한다() {
        String token = provider.createToken("session-a");

        assertInvalid(session("session-b", token), token);
    }

    @Test
    void 서명이_변조되면_저장값까지_같아도_거부한다() {
        String token = provider.createToken("session-a");
        String tampered = token.substring(0, 44) + (token.charAt(44) == 'A' ? 'B' : 'A') + token.substring(45);

        assertInvalid(session("session-a", tampered), tampered);
    }

    @Test
    void 난수가_변조되면_저장값까지_같아도_거부한다() {
        String token = provider.createToken("session-a");
        String tampered = (token.charAt(0) == 'A' ? 'B' : 'A') + token.substring(1);

        assertInvalid(session("session-a", tampered), tampered);
    }

    @Test
    void 다른_비밀키로_서명한_토큰은_거부한다() {
        String token = new WebCsrfTokenProvider("another-csrf-test-key-at-least-32-bytes").createToken("session-a");

        assertInvalid(session("session-a", token), token);
    }

    @Test
    void 같은_세션에_유효한_서명이어도_서버의_현재_토큰과_다르면_거부한다() {
        String token = provider.createToken("session-a");

        assertInvalid(session("session-a", provider.createToken("session-a")), token);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "unsigned-token", ".", "a.b.c", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa."})
    void 누락되거나_형식이_잘못된_토큰은_거부한다(String token) {
        assertInvalid(session("session-a", provider.createToken("session-a")), token);
    }

    @Test
    void 과도하게_긴_토큰은_거부한다() {
        assertInvalid(session("session-a", provider.createToken("session-a")), "a".repeat(10_000));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "short", "                                "})
    void 비밀키는_비어있지_않은_32바이트_이상이어야_한다(String key) {
        assertThatThrownBy(() -> new WebCsrfTokenProvider(key)).isInstanceOf(IllegalArgumentException.class);
    }

    private void assertInvalid(WebAuthSession session, String token) {
        assertThatThrownBy(() -> provider.validate(session, token)).isInstanceOf(CustomException.class)
            .hasMessage(ApiResponseCode.INVALID_CSRF_TOKEN.getMessage());
    }

    private WebAuthSession session(String id, String token) {
        return new WebAuthSession(id, 1, "refresh-hash", token, "credential-hash", Instant.now().plusSeconds(3600), true);
    }
}
