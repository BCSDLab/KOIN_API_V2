package in.koreatech.koin.domain.user.web.service;

/**
 * 웹 세션 조회 결과. 세션의 도메인 상태만 담고, 쿠키를 어떻게 다룰지는 호출한 쪽(컨트롤러)이 정한다.
 *
 * @param status    세션 상태
 * @param userType  AUTHENTICATED일 때의 회원 유형
 * @param csrfToken AUTHENTICATED일 때의 세션별 CSRF 토큰(쿠키 수명 정보 포함)
 */
public record WebSessionResult(Status status, String userType, WebCsrfToken csrfToken) {

    public enum Status {
        /** 유효한 세션이다. */
        AUTHENTICATED,
        /** 인증 정보가 없거나 현재 판단할 수 없다. 브라우저의 쿠키는 그대로 둔다. */
        ANONYMOUS,
        /** 쿠키는 남았지만 서버 세션이 확정적으로 사라졌거나 쿠키 형식이 잘못되었다. 쿠키는 더 쓸모가 없다. */
        SESSION_LOST
    }

    static WebSessionResult authenticated(String userType, WebCsrfToken csrfToken) {
        return new WebSessionResult(Status.AUTHENTICATED, userType, csrfToken);
    }

    static WebSessionResult anonymous() {
        return new WebSessionResult(Status.ANONYMOUS, null, null);
    }

    static WebSessionResult sessionLost() {
        return new WebSessionResult(Status.SESSION_LOST, null, null);
    }

    @Override
    public String toString() {
        return "WebSessionResult[REDACTED]";
    }
}
