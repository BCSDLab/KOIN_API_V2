package in.koreatech.koin.domain.user.web.service;

import in.koreatech.koin.domain.user.web.dto.WebSessionResponse;

/**
 * 웹 세션 조회 결과.
 *
 * @param csrfToken    세션이 유효할 때 복구해 줄 CSRF 쿠키 값. 없으면 null
 * @param clearCookies 세션이 확정적으로 사라져 브라우저의 인증 쿠키를 지워야 하는지 여부
 */
public record WebSessionResult(WebSessionResponse response, WebCsrfToken csrfToken, boolean clearCookies) {

    static WebSessionResult authenticated(String userType, WebCsrfToken csrfToken) {
        return new WebSessionResult(new WebSessionResponse(true, userType, csrfToken.value()), csrfToken, false);
    }

    static WebSessionResult anonymous(boolean clearCookies) {
        return new WebSessionResult(WebSessionResponse.anonymous(), null, clearCookies);
    }

    @Override
    public String toString() {
        return "WebSessionResult[REDACTED]";
    }
}
