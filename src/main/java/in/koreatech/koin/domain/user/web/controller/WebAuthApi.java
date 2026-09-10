package in.koreatech.koin.domain.user.web.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;
import in.koreatech.koin.domain.user.web.dto.WebCsrfTokenResponse;
import in.koreatech.koin.domain.user.web.dto.WebLoginRequest;
import in.koreatech.koin.global.auth.WebAuthRequestValidator;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(
    name = "(Normal) Web Auth: 웹 인증",
    description = "HttpOnly 쿠키 기반 인증. 허용된 Origin 또는 Referer가 필요합니다."
)
@RequestMapping("/v2/web/auth")
public interface WebAuthApi {

    @Operation(
        summary = "웹 로그인",
        description = "토큰은 Set-Cookie로만 발급합니다. 요청에는 credentials를 포함해야 합니다."
    )
    @ApiResponse(responseCode = "201", description = "쿠키 발급 성공")
    @ApiResponseCodes({ApiResponseCode.INVALID_REQUEST_BODY, ApiResponseCode.FORBIDDEN_WEB_ORIGIN})
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<WebAuthResponse> login(
        @RequestBody @Valid WebLoginRequest request,
        HttpServletResponse response
    );

    @Operation(
        summary = "웹 토큰 재발급",
        description = "refresh 쿠키와 CSRF 헤더가 필요합니다. refresh 토큰을 교체하며 로그인 유지 기간은 연장하지 않습니다."
    )
    @ApiResponse(responseCode = "201", description = "쿠키 재발급 성공")
    @ApiResponseCodes({ApiResponseCode.UNAUTHORIZED_USER, ApiResponseCode.INVALID_CSRF_TOKEN,
        ApiResponseCode.FORBIDDEN_WEB_ORIGIN, ApiResponseCode.WEB_AUTH_SESSION_CONFLICT})
    @PostMapping("/refresh")
    ResponseEntity<WebAuthResponse> refresh(
        HttpServletRequest request,
        HttpServletResponse response,
        @Parameter(description = "로그인 또는 /csrf 응답으로 받은 CSRF 토큰")
        @RequestHeader(value = WebAuthRequestValidator.CSRF_HEADER, required = false) String csrfToken
    );

    @Operation(
        summary = "웹 로그아웃",
        description = "현재 웹 세션을 폐기하고 쿠키를 삭제합니다. 다른 웹 세션과 앱 로그인은 유지합니다."
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @ApiResponseCodes({ApiResponseCode.UNAUTHORIZED_USER, ApiResponseCode.INVALID_CSRF_TOKEN,
        ApiResponseCode.FORBIDDEN_WEB_ORIGIN, ApiResponseCode.WEB_AUTH_SESSION_CONFLICT})
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(value = WebAuthRequestValidator.CSRF_HEADER, required = false) String csrfToken
    );

    @Operation(
        summary = "웹 CSRF 토큰 조회",
        description = "페이지 새로고침 후 refresh 쿠키로 CSRF 토큰을 조회합니다. access 토큰 만료 여부와 무관합니다."
    )
    @ApiResponse(responseCode = "200", description = "CSRF 토큰 조회 성공")
    @ApiResponseCodes({ApiResponseCode.UNAUTHORIZED_USER, ApiResponseCode.FORBIDDEN_WEB_ORIGIN})
    @GetMapping("/csrf")
    ResponseEntity<WebCsrfTokenResponse> getCsrfToken(HttpServletRequest request);
}
