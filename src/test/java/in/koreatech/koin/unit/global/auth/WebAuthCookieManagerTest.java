package in.koreatech.koin.unit.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;
import in.koreatech.koin.domain.user.web.service.WebAuthTokens;
import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.config.WebAuthProperties;

class WebAuthCookieManagerTest {

    @Test
    void 로컬_HTTP에서는_보안_접두어_없는_HttpOnly_쿠키를_사용한다() {
        WebAuthProperties properties = new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "Lax");
        WebAuthCookieManager manager = new WebAuthCookieManager(properties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        manager.write(response, new WebAuthTokens("access", "refresh", Instant.now().plusSeconds(900),
            Instant.now().plusSeconds(3600), true, new WebAuthResponse("GENERAL", "csrf")));

        assertThat(response.getCookie("koin-web-access").isHttpOnly()).isTrue();
        assertThat(response.getCookie("koin-web-access").getSecure()).isFalse();
        assertThat(response.getCookie("koin-web-refresh").getPath()).isEqualTo("/v2/web/auth");
    }

    @Test
    void SameSite_None은_Secure_없이는_설정할_수_없다() {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "None"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refresh_기간보다_긴_access_기간은_설정할_수_없다() {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofDays(91), Duration.ofDays(90), true, "Lax"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 잘못된_SameSite_설정을_거부한다() {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), true, "invalid"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
