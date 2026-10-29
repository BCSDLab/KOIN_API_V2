package in.koreatech.koin.unit.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;
import in.koreatech.koin.domain.user.web.service.WebAuthTokens;
import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.config.WebAuthProperties;
import jakarta.servlet.http.Cookie;

class WebAuthProfileTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withInitializer(new ConfigDataApplicationContextInitializer())
        .withUserConfiguration(PropertiesConfig.class);

    @ParameterizedTest
    @CsvSource({"prod,koreatech.in,koin-web", "dev,stage.koreatech.in,koin-stage-web"})
    void 환경별_공유_범위와_쿠키_이름으로_발급하고_같은_범위에서_삭제한다(
        String profile, String domain, String prefix
    ) {
        WebAuthProperties properties = properties(profile);
        assertThat(properties.sharedCookieDomain()).isEqualTo(domain);
        assertThat(properties.cookieNamePrefix()).isEqualTo(prefix);
        assertThat(properties.secure()).isTrue();
        WebAuthCookieManager manager = new WebAuthCookieManager(properties);
        MockHttpServletResponse response = new MockHttpServletResponse();
        manager.write(response, new WebAuthTokens("access", "refresh", Instant.now().plusSeconds(900),
            Instant.now().plusSeconds(3600), true, new WebAuthResponse("GENERAL", "csrf")));

        assertThat(properties.accessCookieName()).isEqualTo("__Secure-" + prefix + "-access");
        assertThat(response.getCookie(properties.accessCookieName()).getDomain()).isEqualTo(domain);
        assertThat(response.getCookie(properties.csrfCookieName()).getDomain()).isEqualTo(domain);
        assertThat(response.getCookie(properties.csrfCookieName()).isHttpOnly()).isFalse();
        assertThat(response.getCookie(properties.refreshCookieName()).getDomain()).isNull();
        assertThat(response.getCookie(properties.refreshCookieName()).getPath()).isEqualTo("/v2/web/auth");

        MockHttpServletResponse cleared = new MockHttpServletResponse();
        manager.clear(cleared);
        for (Cookie cookie : response.getCookies()) {
            Cookie removed = cleared.getCookie(cookie.getName());
            assertThat(removed.getMaxAge()).isZero();
            assertThat(removed.getDomain()).isEqualTo(cookie.getDomain());
            assertThat(removed.getPath()).isEqualTo(cookie.getPath());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "test"})
    void 로컬과_테스트는_배포_도메인을_공유하지_않는다(String profile) {
        assertThat(properties(profile).sharedCookieDomain()).isNull();
    }

    @Test
    void 운영과_stage_쿠키가_같이_전달되어도_자기_환경의_쿠키만_읽는다() {
        WebAuthProperties prod = properties("prod");
        WebAuthProperties stage = properties("dev");
        WebAuthCookieManager prodManager = new WebAuthCookieManager(prod);
        WebAuthCookieManager stageManager = new WebAuthCookieManager(stage);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(prod.accessCookieName(), "prod-access"),
            new Cookie(stage.accessCookieName(), "stage-access"),
            new Cookie(prod.refreshCookieName(), "prod-refresh"),
            new Cookie(stage.refreshCookieName(), "stage-refresh"));

        assertThat(prodManager.getAccessToken(request)).isEqualTo("prod-access");
        assertThat(stageManager.getAccessToken(request)).isEqualTo("stage-access");
        assertThat(prodManager.getRefreshToken(request)).isEqualTo("prod-refresh");
        assertThat(stageManager.getRefreshToken(request)).isEqualTo("stage-refresh");

        request.setCookies(new Cookie(prod.accessCookieName(), "prod-access"));
        assertThat(stageManager.getAccessToken(request)).isNull();
        request.setCookies(new Cookie(stage.accessCookieName(), "stage-access"));
        assertThat(prodManager.getAccessToken(request)).isNull();
    }

    @Test
    void 배포_설정으로_프로필의_기본_공유_범위와_이름을_덮어쓸_수_있다() {
        WebAuthProperties properties = properties("prod",
            "WEB_AUTH_SHARED_COOKIE_DOMAIN=example.test", "WEB_AUTH_COOKIE_NAME_PREFIX=custom-web");

        assertThat(properties.sharedCookieDomain()).isEqualTo("example.test");
        assertThat(properties.cookieNamePrefix()).isEqualTo("custom-web");
    }

    @Test
    void 빈_공유_범위로_프로필의_공유_쿠키를_비활성화할_수_있다() {
        assertThat(properties("prod", "WEB_AUTH_SHARED_COOKIE_DOMAIN=").sharedCookieDomain()).isNull();
    }

    private WebAuthProperties properties(String profile, String... overrides) {
        AtomicReference<WebAuthProperties> properties = new AtomicReference<>();
        runner.withPropertyValues("spring.profiles.active=" + profile).withPropertyValues(overrides).run(context -> {
            assertThat(context).hasNotFailed();
            properties.set(context.getBean(WebAuthProperties.class));
        });
        return properties.get();
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(WebAuthProperties.class)
    static class PropertiesConfig {
    }
}
