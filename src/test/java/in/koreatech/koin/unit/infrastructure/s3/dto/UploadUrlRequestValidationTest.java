package in.koreatech.koin.unit.infrastructure.s3.dto;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import in.koreatech.koin.domain.upload.controller.UploadController;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.exception.GlobalExceptionHandler;
import in.koreatech.koin.infrastructure.s3.dto.UploadUrlResponse;
import in.koreatech.koin.infrastructure.s3.service.UploadService;

class UploadUrlRequestValidationTest {

    private final UploadService uploadService = mock(UploadService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        HandlerMethodArgumentResolver authResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(Auth.class);
            }

            @Override
            public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
            ) {
                return 1;
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new UploadController(uploadService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(authResolver)
            .setValidator(validator)
            .build();

        when(uploadService.getPresignedUrl(any(), any())).thenReturn(
            new UploadUrlResponse(
                "https://s3.example/pre-signed",
                "https://static.example/upload.png",
                java.time.LocalDateTime.of(2026, 9, 2, 12, 0)
            )
        );
    }

    @Test
    void 모든_필수_필드가_없으면_표준_검증_오류를_반환한다() throws Exception {
        mockMvc.perform(post("/OWNERS/upload/url")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.errorTraceId").isNotEmpty())
            .andExpect(jsonPath("$.fieldErrors").isArray())
            .andExpect(jsonPath("$.fieldErrors", hasSize(3)))
            .andExpect(jsonPath("$.fieldErrors[*].field", containsInAnyOrder(
                "content_length", "content_type", "file_name")));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void 파일_크기는_양수여야_한다(int contentLength) throws Exception {
        mockMvc.perform(post("/OWNERS/upload/url")
                .contentType(APPLICATION_JSON)
                .content(validBody(contentLength, "image/png", "hello.png")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
            .andExpect(jsonPath("$.fieldErrors[0].field").value("content_length"))
            .andExpect(jsonPath("$.fieldErrors[0].constraint").value("Positive"));
    }

    @Test
    void 파일_타입과_이름은_공백일_수_없다() throws Exception {
        mockMvc.perform(post("/OWNERS/upload/url")
                .contentType(APPLICATION_JSON)
                .content(validBody(1000, " ", " ")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST_BODY"))
            .andExpect(jsonPath("$.fieldErrors", hasSize(2)))
            .andExpect(jsonPath("$.fieldErrors[*].field", containsInAnyOrder(
                "content_type", "file_name")));
    }

    @Test
    void 세_필드가_유효하면_기존_업로드_URL_응답을_반환한다() throws Exception {
        mockMvc.perform(post("/OWNERS/upload/url")
                .contentType(APPLICATION_JSON)
                .content(validBody(1000, "application/octet-stream", "hello.bin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pre_signed_url").value("https://s3.example/pre-signed"));
    }

    private String validBody(Integer contentLength, String contentType, String fileName) {
        return "{\"content_length\":%d,\"content_type\":\"%s\",\"file_name\":\"%s\"}"
            .formatted(contentLength, contentType, fileName);
    }
}
