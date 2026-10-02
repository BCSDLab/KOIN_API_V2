package in.koreatech.koin.domain.user.web.controller;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;
import in.koreatech.koin.domain.user.web.dto.WebLoginRequest;
import in.koreatech.koin.domain.user.web.dto.WebSessionResponse;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.domain.user.web.service.WebAuthTokens;
import in.koreatech.koin.domain.user.web.service.WebSessionResult;
import in.koreatech.koin.domain.user.web.service.WebSessionService;
import in.koreatech.koin.global.auth.WebAuthCookieManager;
import in.koreatech.koin.global.auth.WebAuthRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/web/auth")
public class WebAuthController implements WebAuthApi {

    private final WebAuthService webAuthService;
    private final WebSessionService webSessionService;
    private final WebAuthCookieManager cookieManager;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WebAuthResponse> login(
        @RequestBody @Valid WebLoginRequest request,
        HttpServletResponse response
    ) {
        WebAuthTokens tokens = webAuthService.login(request);
        cookieManager.write(response, tokens);
        return ResponseEntity.created(URI.create("/")).body(tokens.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<WebAuthResponse> refresh(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(value = WebAuthRequestValidator.CSRF_HEADER, required = false) String csrfToken
    ) {
        WebAuthTokens tokens = webAuthService.refresh(cookieManager.getRefreshToken(request), csrfToken);
        cookieManager.write(response, tokens);
        return ResponseEntity.created(URI.create("/")).body(tokens.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        HttpServletRequest request,
        HttpServletResponse response,
        @RequestHeader(value = WebAuthRequestValidator.CSRF_HEADER, required = false) String csrfToken
    ) {
        webAuthService.logout(cookieManager.getRefreshToken(request), csrfToken);
        cookieManager.clear(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public ResponseEntity<WebSessionResponse> getSession(HttpServletRequest request, HttpServletResponse response) {
        WebSessionResult result = webSessionService.getSession(
            cookieManager.getRefreshToken(request), cookieManager.getAccessToken(request));
        switch (result.status()) {
            case AUTHENTICATED -> {
                cookieManager.writeCsrfToken(response, result.csrfToken());
                return ResponseEntity.ok(new WebSessionResponse(true, result.userType(), result.csrfToken().value()));
            }
            case SESSION_LOST -> cookieManager.clear(response);
            case ANONYMOUS -> { }
        }
        return ResponseEntity.ok(WebSessionResponse.anonymous());
    }
}
