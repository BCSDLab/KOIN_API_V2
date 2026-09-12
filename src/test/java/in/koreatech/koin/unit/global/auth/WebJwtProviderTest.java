package in.koreatech.koin.unit.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.unit.fixture.UserFixture;

class WebJwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider("web-auth-unit-test-key-32-characters", 600_000L);
    private final User user = UserFixture.id_설정_코인_유저(1);

    @Test
    void 기존_앱_토큰은_기존_검증을_통과하고_웹_토큰으로는_인정하지_않는다() {
        String token = jwtProvider.createToken(user);

        assertThat(jwtProvider.getUserId(token)).isEqualTo(user.getId());
        assertThatThrownBy(() -> jwtProvider.getWebTokenClaims(token)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 웹_토큰을_기존_헤더_인증으로_우회할_수_없다() {
        String token = jwtProvider.createWebToken(user, "web-session", Instant.now().plusSeconds(600));

        assertThat(jwtProvider.getWebTokenClaims(token)).isEqualTo(new JwtProvider.WebTokenClaims(1, "web-session"));
        assertThatThrownBy(() -> jwtProvider.getUserId(token)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 만료된_웹_토큰을_거부한다() {
        String token = jwtProvider.createWebToken(user, "web-session", Instant.now().minusSeconds(1));

        assertThatThrownBy(() -> jwtProvider.getWebTokenClaims(token)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 변조된_토큰을_거부하고_예외에_토큰을_노출하지_않는다() {
        String token = "invalid-sensitive-token";

        assertThatThrownBy(() -> jwtProvider.getWebTokenClaims(token))
            .isInstanceOfSatisfying(AuthenticationException.class,
                exception -> assertThat(exception.getFullMessage()).doesNotContain(token));
    }

    @Test
    void 임시_가입_토큰의_기존_계약을_유지한다() {
        assertThat(jwtProvider.getUserId(jwtProvider.createTemporaryToken())).isZero();
    }
}
