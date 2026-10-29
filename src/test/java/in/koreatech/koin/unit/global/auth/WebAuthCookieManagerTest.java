package in.koreatech.koin.unit.global.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.mock.web.MockHttpServletResponse;

import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;
import in.koreatech.koin.domain.user.web.service.WebAuthTokens;
import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.config.WebAuthProperties;

class WebAuthCookieManagerTest {

    @ParameterizedTest
    @ValueSource(strings = {"https://example.test", "example.test:443", "example.test/path", "*.example.test", "-bad.test", "test."})
    void 올바르지_않은_공유_도메인은_거부한다(String domain) {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), true,
            "Lax", domain, "koin-web")).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "__Host-custom", "a;b", "name=token"})
    void 올바르지_않은_쿠키_접두어는_거부한다(String prefix) {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), true,
            "Lax", null, prefix)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 공유_범위는_access와_csrf에만_적용하고_refresh는_API_호스트에_유지한다() {
        WebAuthProperties properties = new Binder(new MapConfigurationPropertySource(Map.of(
            "auth.web.shared-cookie-domain", ".example.test",
            "auth.web.cookie-name-prefix", "koin-stage-web"
        ))).bind("auth.web", Bindable.of(WebAuthProperties.class)).get();
        WebAuthCookieManager manager = new WebAuthCookieManager(properties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        manager.write(response, new WebAuthTokens("access", "refresh", Instant.now().plusSeconds(900),
            Instant.now().plusSeconds(3600), true, new WebAuthResponse("GENERAL", "signed-csrf")));

        assertThat(properties.accessCookieName()).isEqualTo("__Secure-koin-stage-web-access");
        assertThat(response.getCookie(properties.accessCookieName()).getDomain()).isEqualTo("example.test");
        assertThat(response.getCookie(properties.csrfCookieName()).getDomain()).isEqualTo("example.test");
        assertThat(response.getCookie(properties.refreshCookieName()).getDomain()).isNull();
        assertThat(response.getCookie(properties.csrfCookieName()).isHttpOnly()).isFalse();

        MockHttpServletResponse cleared = new MockHttpServletResponse();
        manager.clear(cleared);
        for (String name : new String[] {properties.accessCookieName(), properties.refreshCookieName(), properties.csrfCookieName()}) {
            assertThat(cleared.getCookie(name).getMaxAge()).isZero();
            assertThat(cleared.getCookie(name).getDomain()).isEqualTo(response.getCookie(name).getDomain());
            assertThat(cleared.getCookie(name).getPath()).isEqualTo(response.getCookie(name).getPath());
        }
    }

    @Test
    void csrf는_자바스크립트로_읽을_수_있는_쿠키로_발급한다() {
        WebAuthCookieManager manager = new WebAuthCookieManager(
            new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "Lax", null, "koin-web"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        manager.write(response, new WebAuthTokens("access", "refresh", Instant.now().plusSeconds(900),
            Instant.now().plusSeconds(3600), true, new WebAuthResponse("GENERAL", "signed-csrf")));

        assertThat(response.getCookie("koin-web-csrf")).isNotNull();
        assertThat(response.getCookie("koin-web-csrf").getValue()).isEqualTo("signed-csrf");
        assertThat(response.getCookie("koin-web-csrf").isHttpOnly()).isFalse();
        assertThat(response.getCookie("koin-web-csrf").getPath()).isEqualTo("/");
        assertThat(response.getCookie("koin-web-csrf").getMaxAge()).isBetween(3590, 3600);
        assertThat(response.getCookie("koin-web-access").isHttpOnly()).isTrue();
        assertThat(response.getCookie("koin-web-refresh").isHttpOnly()).isTrue();
    }

    @Test
    void 로그아웃은_csrf_쿠키도_만료시킨다() {
        WebAuthCookieManager manager = new WebAuthCookieManager(
            new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "Lax", null, "koin-web"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        manager.clear(response);

        assertThat(response.getCookie("koin-web-csrf")).isNotNull();
        assertThat(response.getCookie("koin-web-csrf").getMaxAge()).isZero();
    }

    @Test
    void 로컬_HTTP에서는_보안_접두어_없는_HttpOnly_쿠키를_사용한다() {
        WebAuthProperties properties = new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "Lax", null, "koin-web");
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
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "None", null, "koin-web"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refresh_기간보다_긴_access_기간은_설정할_수_없다() {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofDays(91), Duration.ofDays(90), true, "Lax", null, "koin-web"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 잘못된_SameSite_설정을_거부한다() {
        assertThatThrownBy(() -> new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), true, "invalid", null, "koin-web"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
