package in.koreatech.koin.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.global.code.ApiResponseCode;

class ErrorMessageCatalogTest {
    @Test
    void 오류_카탈로그와_언어선택은_응답코드와_검증제약을_보존한다() throws Exception {
        Set<String> expectedKeys = null;
        for (SupportedLanguage language : SupportedLanguage.values()) {
            Properties properties = new Properties();
            try (var input = getClass().getResourceAsStream(
                "/i18n/errors/errors_" + language.getCode() + ".properties")) {
                assertThat(input).isNotNull();
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
            if (expectedKeys == null) {
                expectedKeys = properties.stringPropertyNames();
            }
            assertThat(properties.stringPropertyNames()).isEqualTo(expectedKeys);
            for (ApiResponseCode code : ApiResponseCode.values()) {
                assertThat(properties.getProperty("error." + code.getCode())).isNotBlank();
            }
            assertThat(properties.values()).allSatisfy(value -> assertThat(value.toString()).isNotBlank());
        }

        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test");
        request.addHeader("Accept-Language", "fr;q=1,en-US;q=0.9");
        var result = handler.handleCustomException(request, CustomException.of(ApiResponseCode.SOURCE_CHANGED));
        ErrorResponse response = (ErrorResponse) result.getBody();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.code()).isEqualTo("SOURCE_CHANGED");
        assertThat(response.message()).isEqualTo("The source changed. Reload the original content.");
        assertThat(response.errorTraceId()).isNotBlank();
        request.setParameter("language", "ja-JP");
        assertThat(ErrorMessageCatalog.message(request, ApiResponseCode.SOURCE_CHANGED))
            .isEqualTo("原文が変更されました。再読み込みしてください。");
        request.setParameter("language", "invalid");
        assertThat(ErrorMessageCatalog.message(request, ApiResponseCode.SOURCE_CHANGED))
            .isEqualTo(ApiResponseCode.SOURCE_CHANGED.getMessage());
        request.setParameter("language", "en-US");
        assertThat(ErrorMessageCatalog.message(request, "UNKNOWN_EXTERNAL_CODE", 503, "내부 응답"))
            .isEqualTo("The service is temporarily unavailable.");

        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "displayName", null, false,
            new String[] {"NotBlank"}, null, "이름은 필수입니다."));
        var method = new MethodParameter(Object.class.getMethod("equals", Object.class), 0);
        var exception = new MethodArgumentNotValidException(method, binding);
        var validation = handler.handleMethodArgumentNotValid(exception, HttpHeaders.EMPTY,
            HttpStatus.BAD_REQUEST, new ServletWebRequest(request));
        ErrorResponse validationResponse = (ErrorResponse) validation.getBody();
        assertThat(validationResponse.code()).isEqualTo("INVALID_REQUEST_BODY");
        assertThat(validationResponse.fieldErrors()).singleElement().satisfies(field -> {
            assertThat(field.field()).isEqualTo("display_name");
            assertThat(field.constraint()).isEqualTo("NotBlank");
            assertThat(field.message()).isEqualTo("This field is required.");
        });
        request.setParameter("language", "ko-KR");
        assertThat(ErrorMessageCatalog.validation(request, "NotBlank", "이름은 필수입니다."))
            .isEqualTo("이름은 필수입니다.");
    }
}
