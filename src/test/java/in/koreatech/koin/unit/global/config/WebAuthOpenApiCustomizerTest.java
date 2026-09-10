package in.koreatech.koin.unit.global.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.koreatech.koin.global.config.WebAuthOpenApiCustomizer;
import in.koreatech.koin.global.config.WebAuthProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;

class WebAuthOpenApiCustomizerTest {

    private final WebAuthOpenApiCustomizer customizer = new WebAuthOpenApiCustomizer(
        new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), false, "Strict"));

    @Test
    void 실행_환경의_쿠키_이름과_속성을_문서에_반영한다() {
        Operation refresh = operation();
        OpenAPI openApi = new OpenAPI().paths(new Paths()
            .addPathItem("/v2/web/auth/refresh", new PathItem().post(refresh)));

        customizer.customise(openApi);

        assertThat(openApi.getComponents().getSecuritySchemes().get("WebRefreshCookie").getName())
            .isEqualTo("koin-web-refresh");
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
        OpenAPI openApi = new OpenAPI().paths(new Paths()
            .addPathItem("/v2/web/auth/login", new PathItem().post(login)));

        customizer.customise(openApi);
        String forbidden = login.getResponses().get("403").getDescription();
        customizer.customise(openApi);

        assertThat(login.getParameters()).extracting("name").containsExactly("Origin", "Referer");
        assertThat(login.getResponses().get("403").getDescription()).isEqualTo(forbidden);
        assertThat(login.getSecurity()).isEmpty();
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
