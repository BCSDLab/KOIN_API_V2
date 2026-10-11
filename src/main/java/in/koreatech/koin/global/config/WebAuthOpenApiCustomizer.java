package in.koreatech.koin.global.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.web.controller.WebAuthController;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.auth.UserId;
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
public class WebAuthOpenApiCustomizer implements GlobalOpenApiCustomizer, GlobalOperationCustomizer {

    private static final String JWT = "Jwt Authentication";
    private static final String WEB_ACCESS_COOKIE = "WebAccessCookie";
    private static final String WEB_REFRESH_COOKIE = "WebRefreshCookie";
    private static final String WEB_CSRF_TOKEN = "WebCsrfToken";
    private static final String AUTH_PATH = WebAuthCookieManager.AUTH_PATH;
    // WebConfig에서 쿠키 인터셉터를 제외하는 기존 앱 인증 경로.
    private static final Set<String> NATIVE_AUTH_PATHS = Set.of(
        "/v2/users/login", "/user/login", "/user/refresh", "/user/logout",
        "/student/login", "/owner/login", "/coop/login", "/admin/user/login");
    private static final String SWAGGER_COOKIE_GUIDE =
        "Swagger에서 쿠키 인증을 테스트하려면 Authorize의 Jwt Authentication을 해제한 뒤 웹 로그인하세요. "
            + "쿠키는 브라우저가 관리하며 같은 출처의 요청에 X-CSRF-Token을 자동으로 보냅니다.";

    private final WebAuthProperties properties;

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (WebAuthController.class.isAssignableFrom(handlerMethod.getBeanType())) {
            return operation;
        }
        var parameters = handlerMethod.getMethodParameters();
        boolean usesAuthentication = Arrays.stream(parameters).anyMatch(parameter ->
            parameter.hasParameterAnnotation(Auth.class) || parameter.hasParameterAnnotation(UserId.class));
        boolean unsupportedRole = Arrays.stream(parameters)
            .map(parameter -> parameter.getParameterAnnotation(Auth.class))
            .filter(auth -> auth != null)
            .anyMatch(auth -> Arrays.stream(auth.permit()).noneMatch(UserType.KOIN_USER_TYPES::contains));
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequestMapping.class);
        if (!usesAuthentication || unsupportedRole || mapping == null || isExcludedPath(handlerMethod, mapping)) {
            return operation;
        }
        List<SecurityRequirement> security = operation.getSecurity() == null
            ? new ArrayList<>(List.of(new SecurityRequirement().addList(JWT)))
            : new ArrayList<>(operation.getSecurity());
        if (security.stream().anyMatch(requirement -> requirement.containsKey(WEB_ACCESS_COOKIE))) {
            return operation;
        }
        SecurityRequirement cookie = new SecurityRequirement().addList(WEB_ACCESS_COOKIE);
        if (mapping.method().length == 0 || Arrays.stream(mapping.method()).anyMatch(method ->
            method != RequestMethod.GET && method != RequestMethod.HEAD && method != RequestMethod.OPTIONS)) {
            cookie.addList(WEB_CSRF_TOKEN);
        }
        security.add(cookie);
        boolean optional = Arrays.stream(parameters)
            .noneMatch(parameter -> parameter.hasParameterAnnotation(Auth.class));
        if (optional && security.stream().noneMatch(SecurityRequirement::isEmpty)) {
            security.add(new SecurityRequirement());
        }
        operation.setSecurity(security);
        addCookieGuide(operation);
        return operation;
    }

    private boolean isExcludedPath(HandlerMethod handlerMethod, RequestMapping mapping) {
        RequestMapping typeMapping = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequestMapping.class);
        String[] prefixes = typeMapping == null || typeMapping.path().length == 0 ? new String[] {""} : typeMapping.path();
        for (String prefix : prefixes) {
            for (String path : mapping.path()) {
                String fullPath = prefix + path;
                if (NATIVE_AUTH_PATHS.contains(fullPath) || fullPath.equals("/internal/dining/soldout-reports")
                    || fullPath.startsWith("/internal/dining/soldout-reports/")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getPaths() == null) {
            return;
        }
        configureOperation(openApi, "login", PathItem.HttpMethod.POST, List.of(), "201");
        configureOperation(openApi, "refresh", PathItem.HttpMethod.POST,
            List.of(cookieSecurity(true)), "201");
        configureOperation(openApi, "logout", PathItem.HttpMethod.POST,
            List.of(cookieSecurity(true), new SecurityRequirement()), "204");
        configureOperation(openApi, "session", PathItem.HttpMethod.GET,
            List.of(cookieSecurity(false), new SecurityRequirement()), "200");
        configureSecuritySchemes(openApi);
    }

    private void configureSecuritySchemes(OpenAPI openApi) {
        Set<String> schemes = openApi.getPaths().values().stream()
            .flatMap(path -> path.readOperations().stream())
            .filter(operation -> operation.getSecurity() != null)
            .flatMap(operation -> operation.getSecurity().stream())
            .flatMap(requirement -> requirement.keySet().stream())
            .collect(Collectors.toSet());
        if (schemes.contains(WEB_ACCESS_COOKIE) || schemes.contains(WEB_REFRESH_COOKIE) || schemes.contains(WEB_CSRF_TOKEN)) {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            if (schemes.contains(WEB_ACCESS_COOKIE)) {
                openApi.getComponents().addSecuritySchemes(WEB_ACCESS_COOKIE, new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name(properties.accessCookieName())
                    .description("웹 access 쿠키. 웹 로그인 후 브라우저가 자동 전송합니다."));
            }
            if (schemes.contains(WEB_REFRESH_COOKIE)) {
                openApi.getComponents().addSecuritySchemes(WEB_REFRESH_COOKIE, new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.COOKIE).name(properties.refreshCookieName())
                    .description("웹 refresh 쿠키. 웹 로그인 후 브라우저가 자동 전송합니다."));
            }
            if (schemes.contains(WEB_CSRF_TOKEN)) {
                openApi.getComponents().addSecuritySchemes(WEB_CSRF_TOKEN, new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name(WebAuthRequestValidator.CSRF_HEADER)
                    .description("쿠키 인증의 변경 요청에 필수. Swagger가 같은 출처의 CSRF 쿠키에서 읽어 자동 전송합니다."));
            }
        }
    }

    private void configureOperation(OpenAPI openApi, String endpoint, PathItem.HttpMethod method,
        List<SecurityRequirement> security, String successStatus) {
        PathItem path = openApi.getPaths().get(AUTH_PATH + "/" + endpoint);
        Operation operation = path == null ? null : path.readOperationsMap().get(method);
        if (operation == null) {
            return;
        }
        // 기존 전역 Bearer 설정은 유지하고 웹 인증 API의 별도 조건만 지정한다.
        operation.setSecurity(security);
        addCookieGuide(operation);
        addHeader(operation, HttpHeaders.ORIGIN,
            "허용된 웹 출처. Origin 또는 Referer 필수이며 Origin을 우선 검증합니다.");
        addHeader(operation, HttpHeaders.REFERER,
            "Origin 헤더가 없는 경우 검증할 요청 출처");
        if ((endpoint.equals("refresh") || endpoint.equals("logout")) && operation.getParameters() != null) {
            // CSRF 필수 조건은 security에 명세해 자동 주입 전의 빈 헤더 입력 검증을 피한다.
            operation.getParameters().removeIf(parameter ->
                WebAuthRequestValidator.CSRF_HEADER.equals(parameter.getName()) && "header".equals(parameter.getIn()));
        }
        configureResponses(operation, successStatus);
    }

    private void addCookieGuide(Operation operation) {
        if (operation.getDescription() == null || !operation.getDescription().contains(SWAGGER_COOKIE_GUIDE)) {
            operation.setDescription(operation.getDescription() == null ? SWAGGER_COOKIE_GUIDE
                : operation.getDescription() + "\n" + SWAGGER_COOKIE_GUIDE);
        }
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
            success.addHeaderObject(HttpHeaders.SET_COOKIE,
                cookieHeader(successStatus.equals("204"), successStatus.equals("200")));
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

    private Header cookieHeader(boolean clear, boolean csrfOnly) {
        String description = clear ? "access·refresh·CSRF 쿠키 만료 (Max-Age=0)"
            : "access·refresh는 HttpOnly, CSRF는 일반 쿠키로 발급합니다. auto_login=true이면 Max-Age를 설정합니다.";
        String csrf = cookie(properties.csrfCookieName(), clear ? "" : "SIGNED_CSRF_TOKEN_PLACEHOLDER", "/", clear, false);
        return new Header().description((csrfOnly ? "세션이 유효하면 CSRF 쿠키만 복구합니다(토큰은 회전하지 않음). 세션이 사라졌으면 세 쿠키를 만료시킵니다." : description)
                + " refresh는 API 호스트 전용이며 access·CSRF의 Domain과 쿠키 이름·Secure·SameSite는 서버 설정에 따릅니다.")
            .schema(new ArraySchema().items(new StringSchema()))
            .example(csrfOnly ? List.of(csrf) : List.of(
                cookie(properties.accessCookieName(), clear ? "" : "ACCESS_TOKEN_PLACEHOLDER", "/", clear, true),
                cookie(properties.refreshCookieName(), clear ? "" : "REFRESH_TOKEN_PLACEHOLDER", AUTH_PATH, clear, true),
                csrf
            ));
    }

    private String cookie(String name, String value, String path, boolean clear, boolean httpOnly) {
        return ResponseCookie.from(name, value).path(path).httpOnly(httpOnly)
            .domain(AUTH_PATH.equals(path) ? null : properties.sharedCookieDomain())
            .secure(properties.secure()).sameSite(properties.sameSite()).maxAge(clear ? 0 : -1).build().toString();
    }

    private SecurityRequirement cookieSecurity(boolean csrfRequired) {
        SecurityRequirement security = new SecurityRequirement().addList(WEB_REFRESH_COOKIE);
        return csrfRequired ? security.addList(WEB_CSRF_TOKEN) : security;
    }
}
