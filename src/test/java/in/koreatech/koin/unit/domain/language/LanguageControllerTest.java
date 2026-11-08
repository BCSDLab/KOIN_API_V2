package in.koreatech.koin.unit.domain.language;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import in.koreatech.koin.domain.language.controller.LanguageController;

class LanguageControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new LanguageController()).build();

    @ParameterizedTest
    @ValueSource(strings = {"ko-KR", "en-US"})
    void 로그인_없이_화면_언어와_관계없이_같은_언어_목록을_순서대로_조회한다(String language) throws Exception {
        mockMvc.perform(get("/languages").header(HttpHeaders.ACCEPT_LANGUAGE, language))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "default_language": "ko-KR",
                  "languages": [
                    {"code": "ko-KR", "name": "한국어"},
                    {"code": "en-US", "name": "English"},
                    {"code": "zh-Hans", "name": "简体中文"},
                    {"code": "mn-MN", "name": "Монгол"},
                    {"code": "uz-Latn", "name": "O‘zbekcha"},
                    {"code": "my-MM", "name": "မြန်မာဘာသာ"},
                    {"code": "ja-JP", "name": "日本語"}
                  ]
                }
                """, true));
    }
}
