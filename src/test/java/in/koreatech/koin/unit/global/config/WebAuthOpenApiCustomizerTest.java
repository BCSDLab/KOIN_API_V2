package in.koreatech.koin.unit.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.method.HandlerMethod;

import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.config.SwaggerConfig;
import in.koreatech.koin.global.config.WebAuthOpenApiCustomizer;
import in.koreatech.koin.global.config.WebAuthProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;

class WebAuthOpenApiCustomizerTest {

    @Test
    void 공유_쿠키_설정과_세션_조회의_CSRF_복구를_문서에_반영한다() {
        WebAuthProperties properties = new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90),
            true, "Lax", "example.test", "koin-stage-web");
        Operation login = operation();
        Operation csrf = new Operation().responses(new ApiResponses().addApiResponse("200", new ApiResponse()));
        OpenAPI openApi = new OpenAPI().paths(new Paths()
            .addPathItem("/v2/web/auth/login", new PathItem().post(login))
            .addPathItem("/v2/web/auth/session", new PathItem().get(csrf)));

        new WebAuthOpenApiCustomizer(properties).customise(openApi);

        List<?> cookies = (List<?>) login.getResponses().get("201").getHeaders().get("Set-Cookie").getExample();
        assertThat(cookies).hasSize(3);
        assertThat(cookies.get(0).toString()).contains("__Secure-koin-stage-web-access", "Domain=example.test", "HttpOnly");
        assertThat(cookies.get(1).toString()).contains("__Secure-koin-stage-web-refresh", "HttpOnly").doesNotContain("Domain=");
        assertThat(cookies.get(2).toString()).contains("__Secure-koin-stage-web-csrf", "Domain=example.test").doesNotContain("HttpOnly");
        List<?> restored = (List<?>) csrf.getResponses().get("200").getHeaders().get("Set-Cookie").getExample();
        assertThat(restored).hasSize(1);
        assertThat(restored.get(0)).isEqualTo(cookies.get(2));
    }

    private final WebAuthOpenApiCustomizer customizer = new WebAuthOpenApiCustomizer(
        new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "Strict", null, "koin-web"));

    @ParameterizedTest
    @ValueSource(strings = {"springdoc.swagger-ui.enabled=false", "springdoc.api-docs.enabled=false"})
    void Swagger를_끄면_UI_설정_빈_없이도_시작한다(String disabledProperty) {
        new ApplicationContextRunner().withUserConfiguration(SwaggerConfig.class)
            .withPropertyValues("swagger.server-url=http://localhost:8080", disabledProperty)
            .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void 실행_환경의_쿠키_이름과_속성을_문서에_반영한다() {
        Operation refresh = operation();
        refresh.addParametersItem(new HeaderParameter().name("X-CSRF-Token").required(true));
        OpenAPI openApi = new OpenAPI().paths(new Paths()
            .addPathItem("/v2/web/auth/refresh", new PathItem().post(refresh)));

        customizer.customise(openApi);

        assertThat(openApi.getComponents().getSecuritySchemes().get("WebRefreshCookie").getName())
            .isEqualTo("koin-web-refresh");
        assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("WebCsrfToken");
        assertThat(openApi.getComponents().getSecuritySchemes().get("WebCsrfToken").getName())
            .isEqualTo("X-CSRF-Token");
        assertThat(refresh.getSecurity()).containsExactly(new SecurityRequirement()
            .addList("WebRefreshCookie").addList("WebCsrfToken"));
        assertThat(refresh.getParameters()).extracting("name").doesNotContain("X-CSRF-Token");
        assertThat(refresh.getResponses().get("201").getHeaders().get("Set-Cookie").getExample().toString())
            .contains("koin-web-access=", "koin-web-refresh=", "HttpOnly", "SameSite=Strict")
            .doesNotContain("__Host-", "__Secure-", "; Secure", "Domain=");
    }

    @Test
    void 웹_인증이_없는_그룹과_기존_Bearer_인증은_변경하지_않는다() {
        Operation nativeLogin = operation();
        List<SecurityRequirement> bearer = List.of(new SecurityRequirement().addList("Jwt Authentication"));
        OpenAPI openApi = new OpenAPI().security(bearer).paths(new Paths()
            .addPathItem("/v2/users/login", new PathItem().post(nativeLogin)));

        customizer.customise(openApi);

        assertThat(openApi.getSecurity()).isEqualTo(bearer);
        assertThat(openApi.getComponents()).isNull();
        assertThat(nativeLogin.getParameters()).isNull();
        assertThat(nativeLogin.getSecurity()).isNull();
        assertThat(nativeLogin.getResponses().get("201").getHeaders()).isNull();
    }

    @Test
    void 반복_적용해도_요청_헤더와_오류_설명이_중복되지_않는다() {
        Operation login = operation();
        Operation refresh = operation().addParametersItem(new HeaderParameter().name("X-CSRF-Token").required(true));
        OpenAPI openApi = new OpenAPI().paths(new Paths()
            .addPathItem("/v2/web/auth/login", new PathItem().post(login))
            .addPathItem("/v2/web/auth/refresh", new PathItem().post(refresh)));

        customizer.customise(openApi);
        String forbidden = login.getResponses().get("403").getDescription();
        customizer.customise(openApi);

        assertThat(login.getParameters()).extracting("name").containsExactly("Origin", "Referer");
        assertThat(login.getResponses().get("403").getDescription()).isEqualTo(forbidden);
        assertThat(login.getSecurity()).isEmpty();
        assertThat(refresh.getParameters()).extracting("name").containsExactly("Origin", "Referer");
        assertThat(refresh.getSecurity()).hasSize(1);
    }

    @Test
    void 허용_유형이_없는_Auth에는_쿠키_인증을_추가하지_않는다() throws Exception {
        Operation operation = operation().addSecurityItem(new SecurityRequirement().addList("Jwt Authentication"));
        HandlerMethod handler = new HandlerMethod(this, getClass().getDeclaredMethod("emptyPermit", Integer.class));

        customizer.customize(operation, handler);

        assertThat(operation.getSecurity()).containsExactly(new SecurityRequirement().addList("Jwt Authentication"));
    }

    @PostMapping("/test/empty-permit")
    private void emptyPermit(@Auth Integer userId) {
    }

    @Test
    void 경로나_응답이_없는_문서에도_적용할_수_있다() {
        assertThatCode(() -> customizer.customise(new OpenAPI())).doesNotThrowAnyException();
        OpenAPI openApi = new OpenAPI().paths(new Paths()
            .addPathItem("/v2/web/auth/login", new PathItem().post(new Operation())));

        assertThatCode(() -> customizer.customise(openApi)).doesNotThrowAnyException();
    }

    private Operation operation() {
        return new Operation().responses(new ApiResponses()
            .addApiResponse("201", new ApiResponse().description("성공"))
            .addApiResponse("403", new ApiResponse().description("출처 거부")));
    }
}
