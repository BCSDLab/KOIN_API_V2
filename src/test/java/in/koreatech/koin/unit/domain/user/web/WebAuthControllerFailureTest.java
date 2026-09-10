package in.koreatech.koin.unit.domain.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import in.koreatech.koin.domain.user.web.controller.WebAuthController;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.WebAuthProperties;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.exception.GlobalExceptionHandler;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class WebAuthControllerFailureTest {

    @Mock
    private WebAuthService service;

    private MockMvc mockMvc;
    private final WebAuthProperties properties = new WebAuthProperties(
        Duration.ofMinutes(15), Duration.ofDays(90), true, "Lax"
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new WebAuthController(service, new WebAuthCookieManager(properties)))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"login", "refresh", "logout", "csrf"})
    void Redis_장애는_서버_오류를_반환하고_기존_쿠키를_덮거나_삭제하지_않는다(String endpoint) throws Exception {
        RedisConnectionFailureException failure = new RedisConnectionFailureException("테스트용 Redis 연결 실패");
        switch (endpoint) {
            case "login" -> when(service.login(any())).thenThrow(failure);
            case "refresh" -> when(service.refresh(anyString(), anyString())).thenThrow(failure);
            case "logout" -> doThrow(failure).when(service).logout(anyString(), anyString());
            case "csrf" -> when(service.getCsrfToken(anyString())).thenThrow(failure);
            default -> throw new IllegalArgumentException("잘못된 테스트 경로");
        }

        MockHttpServletRequestBuilder request = "csrf".equals(endpoint)
            ? get("/v2/web/auth/csrf") : post("/v2/web/auth/" + endpoint);
        mockMvc.perform(request.header("Origin", "http://localhost:3000").header("X-CSRF-Token", "test-csrf")
                .cookie(new Cookie(properties.refreshCookieName(), "test-refresh"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"login_id\":\"test\",\"login_pw\":\"test-password\"}"))
            .andExpect(status().isInternalServerError())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void 동시_재발급_충돌은_409를_반환하고_성공한_요청의_쿠키를_덮지_않는다() throws Exception {
        when(service.refresh(anyString(), anyString())).thenThrow(CustomException.of(ApiResponseCode.WEB_AUTH_SESSION_CONFLICT));

        mockMvc.perform(post("/v2/web/auth/refresh").header("X-CSRF-Token", "test-csrf")
                .cookie(new Cookie(properties.refreshCookieName(), "test-refresh")))
            .andExpect(status().isConflict())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }
}
