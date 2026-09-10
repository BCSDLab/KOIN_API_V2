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
import in.koreatech.koin.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "(Normal) User: 유저", description = "유저 관련 API")
@RequestMapping("/v2/web/auth")
public interface WebAuthApi {

    @Operation(
        summary = "웹 로그인",
        description = """
            일반인/학생/총학생회 사용자의 웹 로그인을 처리합니다.
            access 토큰과 refresh 토큰은 HttpOnly 쿠키로 발급하고, 회원 유형과 CSRF 토큰을 반환합니다.
            """
    )
    @ApiResponse(responseCode = "201", description = "쿠키 발급 성공",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = WebAuthResponse.class)))
    @ApiResponse(responseCode = "415", description = "application/json 이외의 Content-Type",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponseCodes({ApiResponseCode.INVALID_REQUEST_BODY, ApiResponseCode.NOT_MATCHED_PASSWORD,
        ApiResponseCode.NOT_READABLE_HTTP_MESSAGE, ApiResponseCode.NOT_FOUND_USER,
        ApiResponseCode.FORBIDDEN_WEB_ORIGIN, ApiResponseCode.INTERNAL_SERVER_ERROR})
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<WebAuthResponse> login(
        @RequestBody @Valid WebLoginRequest request,
        HttpServletResponse response
    );

    @Operation(
        summary = "웹 토큰 재발급",
        description = """
            refresh 쿠키와 CSRF 토큰으로 access 토큰과 refresh 토큰을 재발급합니다.
            refresh 토큰의 최초 만료 시각과 CSRF 토큰은 유지됩니다.
            """
    )
    @ApiResponse(responseCode = "201", description = "쿠키 재발급 성공",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = WebAuthResponse.class)))
    @ApiResponseCodes({ApiResponseCode.UNAUTHORIZED_USER, ApiResponseCode.INVALID_CSRF_TOKEN,
        ApiResponseCode.FORBIDDEN_WEB_ORIGIN, ApiResponseCode.WEB_AUTH_SESSION_CONFLICT,
        ApiResponseCode.INTERNAL_SERVER_ERROR})
    @PostMapping("/refresh")
    ResponseEntity<WebAuthResponse> refresh(
        HttpServletRequest request,
        HttpServletResponse response,
        @Parameter(description = "로그인 또는 CSRF 토큰 조회 응답으로 받은 CSRF 토큰", required = true)
        @RequestHeader(value = WebAuthRequestValidator.CSRF_HEADER, required = false) String csrfToken
    );

    @Operation(
        summary = "웹 로그아웃",
        description = """
            현재 웹 세션을 삭제하고 인증 쿠키를 만료시킵니다. 삭제된 세션의 access 토큰은 사용할 수 없습니다.
            refresh 쿠키가 없으면 쿠키만 만료시키며 서버 세션은 삭제하지 않습니다.
            """
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @ApiResponseCodes({ApiResponseCode.UNAUTHORIZED_USER, ApiResponseCode.INVALID_CSRF_TOKEN,
        ApiResponseCode.FORBIDDEN_WEB_ORIGIN, ApiResponseCode.WEB_AUTH_SESSION_CONFLICT,
        ApiResponseCode.INTERNAL_SERVER_ERROR})
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        @Parameter(description = "CSRF 토큰. refresh 쿠키가 없거나 세션이 이미 삭제된 경우 생략 가능")
        @RequestHeader(value = WebAuthRequestValidator.CSRF_HEADER, required = false) String csrfToken
    );

    @Operation(
        summary = "웹 CSRF 토큰 조회",
        description = """
            refresh 쿠키로 현재 웹 세션의 CSRF 토큰을 조회합니다.
            access 토큰과 refresh 토큰은 재발급하지 않습니다.
            """
    )
    @ApiResponse(responseCode = "200", description = "CSRF 토큰 조회 성공",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = WebCsrfTokenResponse.class)))
    @ApiResponseCodes({ApiResponseCode.UNAUTHORIZED_USER, ApiResponseCode.FORBIDDEN_WEB_ORIGIN,
        ApiResponseCode.INTERNAL_SERVER_ERROR})
    @GetMapping("/csrf")
    ResponseEntity<WebCsrfTokenResponse> getCsrfToken(HttpServletRequest request);
}
