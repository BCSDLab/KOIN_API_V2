package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.JsonNode;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.support.JsonAssertions;

@TestPropertySource(properties = {"article.ai-summary.enabled=false", "upstage.api-key="})
class LanguageApiTest extends AcceptanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer invalid-token"})
    void 지원_언어는_인증_정보가_없거나_만료되어도_조회한다(String authorization) throws Exception {
        mockMvc.perform(get("/languages").header(HttpHeaders.AUTHORIZATION, authorization))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.default_language").value("ko-KR"))
            .andExpect(jsonPath("$.languages.length()").value(7));
    }

    @Test
    void 사용자_Swagger에_목록과_본인_설정의_인증_계약을_노출한다() throws Exception {
        JsonNode openApi = JsonAssertions.convertJsonNode(mockMvc.perform(get("/v3/api-docs/{group}", "4. User API"))
            .andExpect(status().isOk()).andReturn());
        JsonNode languages = openApi.at("/paths/~1languages/get");
        assertThat(languages.isMissingNode()).isFalse();
        assertThat(languages.path("security").isArray()).isTrue();
        assertThat(languages.path("security")).isEmpty();

        JsonNode getLanguage = openApi.at("/paths/~1users~1me~1language/get/security");
        assertThat(getLanguage.get(0).has("Jwt Authentication")).isTrue();
        assertThat(getLanguage.get(1).has("WebAccessCookie")).isTrue();
        JsonNode patchLanguage = openApi.at("/paths/~1users~1me~1language/patch/security");
        assertThat(patchLanguage.get(0).has("Jwt Authentication")).isTrue();
        assertThat(patchLanguage.get(1).has("WebAccessCookie")).isTrue();
        assertThat(patchLanguage.get(1).has("WebCsrfToken")).isTrue();
        assertThat(openApi.path("paths").has("/policies/{type}")).isTrue();
        mockMvc.perform(get("/policies/privacy").param("language", "en-US"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.requested_language").value("en-US"))
            .andExpect(jsonPath("$.language").value("ko-KR"))
            .andExpect(jsonPath("$.is_fallback").value(true)).andExpect(jsonPath("$.sections.length()").value(8));
        mockMvc.perform(get("/policies/terms"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.is_fallback").value(false))
            .andExpect(jsonPath("$.sections.length()").value(16));
        mockMvc.perform(get("/languages/en-US/messages"))
            .andExpect(status().isOk()).andExpect(jsonPath("$['dining.place.A코스']").value("Corner A"));
    }
}
