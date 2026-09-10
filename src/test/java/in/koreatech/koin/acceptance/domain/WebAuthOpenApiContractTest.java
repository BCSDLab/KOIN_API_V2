package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;

class WebAuthOpenApiContractTest extends AcceptanceTest {

    private static final String LOGIN_GROUP = "0. Login API";
    private static final String WEB_REFRESH_COOKIE = "WebRefreshCookie";

    @Test
    void 웹_로그인은_Bearer를_요구하지_않고_기존_앱_설정은_유지한다() throws Exception {
        JsonNode openApi = openApi(LOGIN_GROUP);

        assertThat(operation(openApi, "login").path("security").isArray()).isTrue();
        assertThat(operation(openApi, "login").path("security")).isEmpty();
        assertThat(openApi.path("security").get(0).has("Jwt Authentication")).isTrue();
        assertThat(openApi.at("/paths/~1v2~1users~1login/post").isMissingNode()).isFalse();
    }

    @Test
    void 웹_인증_API는_기존_유저_로그인과_같은_태그에_포함한다() throws Exception {
        JsonNode openApi = openApi(LOGIN_GROUP);
        JsonNode nativeLogin = openApi.at("/paths/~1v2~1users~1login/post");

        for (String endpoint : List.of("login", "refresh", "logout", "csrf")) {
            assertThat(operation(openApi, endpoint).path("tags")).isEqualTo(nativeLogin.path("tags"));
        }
        assertThat(nativeLogin.path("tags").get(0).asText()).isEqualTo("(Normal) User: 유저");
        assertThat(openApi.path("tags"))
            .noneMatch(tag -> tag.path("name").asText().equals("(Normal) Web Auth: 웹 인증"));
    }

    @Test
    void 웹_인증_API는_회원_그룹에_중복_노출하지_않는다() throws Exception {
        JsonNode openApi = openApi("4. User API");

        for (String endpoint : List.of("login", "refresh", "logout", "csrf")) {
            assertThat(operation(openApi, endpoint).isMissingNode()).isTrue();
        }
        assertThat(openApi.at("/paths/~1v2~1users~1login/post").isMissingNode()).isFalse();
        assertThat(openApi.at("/paths/~1v2~1users~1me/get").isMissingNode()).isFalse();
        assertThat(openApi.path("security").get(0).has("Jwt Authentication")).isTrue();
    }

    @Test
    void 웹_인증_출처와_refresh_쿠키를_명세한다() throws Exception {
        JsonNode openApi = openApi(LOGIN_GROUP);

        for (String endpoint : List.of("login", "refresh", "logout", "csrf")) {
            JsonNode operation = operation(openApi, endpoint);
            assertThat(parameter(operation, "Origin").path("in").asText()).isEqualTo("header");
            assertThat(parameter(operation, "Referer").path("in").asText()).isEqualTo("header");
        }
        JsonNode cookieScheme = openApi.path("components").path("securitySchemes").path(WEB_REFRESH_COOKIE);
        assertThat(cookieScheme.path("in").asText()).isEqualTo("cookie");
        assertThat(cookieScheme.path("name").asText()).isEqualTo("__Secure-koin-web-refresh");
        for (String endpoint : List.of("refresh", "csrf")) {
            assertThat(operation(openApi, endpoint).path("security").get(0).has(WEB_REFRESH_COOKIE)).isTrue();
        }
    }

    @Test
    void 재발급과_로그아웃의_CSRF_조건을_구분한다() throws Exception {
        JsonNode openApi = openApi(LOGIN_GROUP);
        JsonNode refresh = operation(openApi, "refresh");
        JsonNode logout = operation(openApi, "logout");

        assertThat(parameter(refresh, "X-CSRF-Token").path("required").asBoolean()).isTrue();
        assertThat(parameter(logout, "X-CSRF-Token").path("required").asBoolean()).isFalse();
        assertThat(logout.path("security")).hasSize(2);
        assertThat(logout.path("security").get(1)).isEmpty();
        assertThat(logout.path("description").asText()).contains("서버 세션은 삭제하지 않습니다");
        assertThat(operation(openApi, "csrf").path("parameters"))
            .noneMatch(parameter -> parameter.path("name").asText().equals("X-CSRF-Token"));
    }

    @Test
    void 쿠키_발급과_삭제_응답을_명세하고_토큰은_JSON에_노출하지_않는다() throws Exception {
        JsonNode openApi = openApi(LOGIN_GROUP);
        for (String endpoint : List.of("login", "refresh")) {
            JsonNode response = operation(openApi, endpoint).at("/responses/201");
            JsonNode cookie = response.at("/headers/Set-Cookie");
            assertThat(cookie.path("schema").path("type").asText()).isEqualTo("array");
            assertThat(cookie.path("example")).hasSize(2);
            assertThat(cookie.path("example").get(0).asText())
                .contains("__Host-koin-web-access", "Path=/", "HttpOnly", "Secure", "SameSite=Lax");
            assertThat(cookie.path("example").get(1).asText())
                .contains("__Secure-koin-web-refresh", "Path=/v2/web/auth", "HttpOnly");
            assertThat(response.at("/content/application~1json/schema/$ref").asText()).endsWith("WebAuthResponse");
        }
        JsonNode logout = operation(openApi, "logout").at("/responses/204");
        assertThat(logout.has("content")).isFalse();
        assertThat(logout.at("/headers/Set-Cookie/example"))
            .allSatisfy(cookie -> assertThat(cookie.asText()).contains("Max-Age=0"));
        String schemaRef = operation(openApi, "login").at("/responses/201/content/application~1json/schema/$ref")
            .asText();
        JsonNode properties = openApi.at(schemaRef.substring(1)).path("properties");
        assertThat(properties).hasSize(2);
        assertThat(properties.has("user_type")).isTrue();
        assertThat(properties.has("csrf_token")).isTrue();
    }

    @Test
    void 실제_로그인_실패와_갱신_충돌_오류를_명세한다() throws Exception {
        JsonNode openApi = openApi(LOGIN_GROUP);
        JsonNode login = operation(openApi, "login");

        assertThat(login.at("/responses/400/content/application~1json/examples").has("NOT_MATCHED_PASSWORD"))
            .isTrue();
        assertThat(login.at("/responses/400/content/application~1json/examples").has("NOT_READABLE_HTTP_MESSAGE"))
            .isTrue();
        assertThat(login.at("/responses/404/content/application~1json/examples").has("NOT_FOUND_USER"))
            .isTrue();
        assertThat(login.at("/responses/415/content/application~1json/schema/$ref").asText())
            .endsWith("ErrorResponse");
        for (String endpoint : List.of("login", "refresh", "logout", "csrf")) {
            assertThat(operation(openApi, endpoint).at("/responses/500/content/application~1json/examples")
                .has("INTERNAL_SERVER_ERROR")).isTrue();
            assertThat(operation(openApi, endpoint).at("/responses/403/content/text~1plain/example").asText())
                .isEqualTo("Invalid CORS request");
        }
        for (String endpoint : List.of("refresh", "logout")) {
            assertThat(operation(openApi, endpoint).at("/responses/409/content/application~1json/examples")
                .has("WEB_AUTH_SESSION_CONFLICT")).isTrue();
            assertThat(operation(openApi, endpoint).at("/responses/403/content/application~1json/examples")
                .has("INVALID_CSRF_TOKEN")).isTrue();
        }
    }

    private JsonNode openApi(String group) throws Exception {
        return JsonAssertions.convertJsonNode(mockMvc.perform(get("/v3/api-docs/{group}", group))
            .andExpect(status().isOk()).andReturn());
    }

    private JsonNode operation(JsonNode openApi, String endpoint) {
        String method = endpoint.equals("csrf") ? "get" : "post";
        return openApi.path("paths").path("/v2/web/auth/" + endpoint).path(method);
    }

    private JsonNode parameter(JsonNode operation, String name) {
        for (JsonNode parameter : operation.path("parameters")) {
            if (parameter.path("name").asText().equals(name)) {
                return parameter;
            }
        }
        throw new AssertionError("명세에 요청 헤더가 없습니다: " + name);
    }
}
