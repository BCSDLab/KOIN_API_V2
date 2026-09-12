package in.koreatech.koin.global.config;

import java.util.List;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.auth.WebAuthRequestValidator;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebAuthOpenApiCustomizer implements GlobalOpenApiCustomizer {

    private static final String WEB_REFRESH_COOKIE = "WebRefreshCookie";
    private static final String AUTH_PATH = WebAuthCookieManager.AUTH_PATH;

    private final WebAuthProperties properties;

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }
        configureOperation(openApi, "login", PathItem.HttpMethod.POST, List.of(), "201");
        configureOperation(openApi, "refresh", PathItem.HttpMethod.POST,
            List.of(cookieSecurity()), "201");
        configureOperation(openApi, "logout", PathItem.HttpMethod.POST,
            List.of(cookieSecurity(), new SecurityRequirement()), "204");
        configureOperation(openApi, "csrf", PathItem.HttpMethod.GET,
            List.of(cookieSecurity()), "200");
    }

    private void configureOperation(OpenAPI openApi, String endpoint, PathItem.HttpMethod method,
        List<SecurityRequirement> security, String successStatus) {
        PathItem path = openApi.getPaths().get(AUTH_PATH + "/" + endpoint);
        Operation operation = path == null ? null : path.readOperationsMap().get(method);
        if (operation == null) {
            return;
        }
        // 웹 인증 API에만 적용하고 기존 앱 API의 전역 Bearer 설정은 유지한다.
        operation.setSecurity(security);
        if (!security.isEmpty()) {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSecuritySchemes(WEB_REFRESH_COOKIE, new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE)
                .name(properties.refreshCookieName())
                .description("웹 refresh 토큰 (브라우저가 관리하는 HttpOnly 쿠키)"));
        }
        addHeader(operation, HttpHeaders.ORIGIN,
            "허용된 웹 출처. Origin 또는 Referer 필수이며 Origin을 우선 검증합니다.");
        addHeader(operation, HttpHeaders.REFERER,
            "Origin 헤더가 없는 경우 검증할 요청 출처");
        if (endpoint.equals("refresh") && operation.getParameters() != null) {
            // 런타임의 required=false는 누락 시 기존 403 오류를 반환하기 위해 유지한다.
            operation.getParameters().stream()
                .filter(parameter -> WebAuthRequestValidator.CSRF_HEADER.equals(parameter.getName()))
                .forEach(parameter -> parameter.setRequired(true));
        }
        configureResponses(operation, successStatus);
    }

    private void addHeader(Operation operation, String name, String description) {
        if (operation.getParameters() != null && operation.getParameters().stream()
            .anyMatch(parameter -> name.equals(parameter.getName()) && "header".equals(parameter.getIn()))) {
            return;
        }
        operation.addParametersItem(new HeaderParameter().name(name).description(description)
            .required(false).schema(new StringSchema()));
    }

    private void configureResponses(Operation operation, String successStatus) {
        if (operation.getResponses() == null) {
            return;
        }
        ApiResponse success = operation.getResponses().get(successStatus);
        if (success != null) {
            success.addHeaderObject(HttpHeaders.CACHE_CONTROL,
                new Header().description("인증 응답 캐시 방지").schema(new StringSchema()).example("no-store"));
            if (successStatus.equals("201") || successStatus.equals("204")) {
                success.addHeaderObject(HttpHeaders.SET_COOKIE, cookieHeader(successStatus.equals("204")));
            }
        }
        ApiResponse forbidden = operation.getResponses().get("403");
        if (forbidden != null) {
            if (forbidden.getContent() == null) {
                forbidden.setContent(new Content());
            }
            forbidden.getContent().addMediaType("text/plain", new MediaType().schema(new StringSchema())
                .example("Invalid CORS request"));
            String description = "CORS 검사에서 차단된 Origin은 text/plain으로 응답합니다.";
            if (forbidden.getDescription() == null || !forbidden.getDescription().contains(description)) {
                forbidden.setDescription(forbidden.getDescription() == null ? description
                    : forbidden.getDescription() + "\n" + description);
            }
        }
    }

    private Header cookieHeader(boolean clear) {
        String description = clear ? "access·refresh 쿠키 만료 (Max-Age=0)"
            : "access·refresh HttpOnly 쿠키 발급. auto_login=true이면 Max-Age를 설정합니다.";
        return new Header().description(description + " Domain 미지정. 쿠키 이름과 Secure·SameSite는 서버 설정에 따릅니다.")
            .schema(new ArraySchema().items(new StringSchema()))
            .example(List.of(
                cookie(properties.accessCookieName(), clear ? "" : "ACCESS_TOKEN_PLACEHOLDER", "/", clear),
                cookie(properties.refreshCookieName(), clear ? "" : "REFRESH_TOKEN_PLACEHOLDER", AUTH_PATH, clear)
            ));
    }

    private String cookie(String name, String value, String path, boolean clear) {
        return ResponseCookie.from(name, value).path(path).httpOnly(true)
            .secure(properties.secure()).sameSite(properties.sameSite()).maxAge(clear ? 0 : -1).build().toString();
    }

    private SecurityRequirement cookieSecurity() {
        return new SecurityRequirement().addList(WEB_REFRESH_COOKIE);
    }
}
