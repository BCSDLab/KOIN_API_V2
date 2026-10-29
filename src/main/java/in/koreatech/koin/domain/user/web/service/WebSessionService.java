package in.koreatech.koin.domain.user.web.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.domain.user.web.repository.WebAuthSessionRedisRepository;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;

/**
 * 웹 로그인 세션 상태를 조회하는 읽기 전용 서비스. 토큰을 회전하지 않고 세션도 변경하지 않는다.
 * SQL 트랜잭션 안에서 Redis를 기다리지 않도록 트랜잭션을 열지 않는다.
 */
@Service
@RequiredArgsConstructor
public class WebSessionService {

    private final WebAuthService webAuthService;
    private final WebAuthSessionRedisRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * refresh 쿠키의 세션을 우선 보고, 일치하지 않으면 access 쿠키로 확인한다. refresh만 남은 상태(access 만료)도
     * 세션이 유효하므로 AUTHENTICATED이며, access는 이후 API 요청의 401에서 재발급된다.
     * SESSION_LOST는 세션이 저장소에서 확정적으로 사라졌거나 쿠키가 형식에 맞지 않을 때뿐이다.
     * refresh 값이 세션과 어긋나는 경우는 다른 탭이 이미 회전시켰을 수 있어 ANONYMOUS로 남긴다.
     */
    public WebSessionResult getSession(String refreshValue, String accessValue) {
        RefreshLookup refresh = lookupByRefreshToken(refreshValue);
        WebAuthSession session = refresh.session();
        if (session == null && StringUtils.hasText(accessValue)) {
            session = webAuthService.findSessionByAccessToken(accessValue).orElse(null);
        }
        if (session == null) {
            return hasCredentials(refreshValue, accessValue) && !refresh.rotated()
                ? WebSessionResult.sessionLost() : WebSessionResult.anonymous();
        }
        User user = userRepository.findById(session.userId()).orElse(null);
        if (user == null) {
            return WebSessionResult.sessionLost();
        }
        return WebSessionResult.authenticated(user.getUserType().getValue(),
            new WebCsrfToken(session.csrfToken(), session.expiresAt(), session.autoLogin()));
    }

    private boolean hasCredentials(String refreshValue, String accessValue) {
        return StringUtils.hasText(refreshValue) || StringUtils.hasText(accessValue);
    }

    private RefreshLookup lookupByRefreshToken(String value) {
        if (!StringUtils.hasText(value)) {
            return RefreshLookup.NONE;
        }
        try {
            WebRefreshToken refreshToken = WebRefreshToken.parse(value);
            Optional<WebAuthSession> found = sessionRepository.findById(refreshToken.sessionId());
            if (found.isEmpty() || found.get().isExpired()) {
                return RefreshLookup.NONE;
            }
            return found.get().matchesRefreshToken(refreshToken) ? new RefreshLookup(found.get(), false)
                : new RefreshLookup(null, true);
        } catch (AuthenticationException e) {
            return RefreshLookup.NONE;
        }
    }

    /** rotated: 세션은 있지만 refresh 값이 다르다(다른 탭이 이미 회전시켰을 수 있다). */
    private record RefreshLookup(WebAuthSession session, boolean rotated) {

        static final RefreshLookup NONE = new RefreshLookup(null, false);
    }
}
