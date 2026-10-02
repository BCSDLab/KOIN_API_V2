package in.koreatech.koin.domain.user.web.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.web.dto.WebAuthResponse;
import in.koreatech.koin.domain.user.web.dto.WebLoginRequest;
import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.domain.user.web.repository.WebAuthSessionRedisRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.WebAuthProperties;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebAuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final WebAuthSessionRedisRepository sessionRepository;
    private final JwtProvider jwtProvider;
    private final WebAuthProperties properties;
    private final WebCsrfTokenProvider csrfTokenProvider;

    @Transactional
    public WebAuthTokens login(WebLoginRequest request) {
        User user = userService.authenticate(request.toLoginRequest());
        WebRefreshToken refreshToken = WebRefreshToken.create();
        WebAuthSession session = new WebAuthSession(
            refreshToken.sessionId(), user.getId(), refreshToken.hash(), csrfTokenProvider.createToken(refreshToken.sessionId()),
            WebRefreshToken.hash(user.getLoginPw()),
            Instant.now().plus(properties.refreshTokenTtl()).truncatedTo(ChronoUnit.SECONDS), request.autoLogin()
        );
        WebAuthTokens tokens = createTokens(user, session, refreshToken);
        sessionRepository.save(session);
        return tokens;
    }

    @Transactional(readOnly = true)
    public WebAuthTokens refresh(String value, String csrfToken) {
        WebRefreshToken refreshToken = WebRefreshToken.parse(value);
        WebAuthSession session = getSession(refreshToken.sessionId());
        session.requireRefreshToken(refreshToken);
        csrfTokenProvider.validate(session, csrfToken);

        User user = userRepository.findById(session.userId())
            .orElseThrow(() -> AuthenticationException.withDetail("웹 로그인 사용자가 존재하지 않습니다."));
        if (!session.credentialHash().equals(WebRefreshToken.hash(user.getLoginPw()))) {
            sessionRepository.delete(session);
            throw AuthenticationException.withDetail("비밀번호가 변경되었습니다. 다시 로그인해주세요.");
        }

        WebRefreshToken nextToken = refreshToken.rotate();
        WebAuthSession nextSession = session.rotate(nextToken);
        WebAuthTokens tokens = createTokens(user, nextSession, nextToken);
        if (!sessionRepository.rotate(session, nextSession)) {
            throw CustomException.of(ApiResponseCode.WEB_AUTH_SESSION_CONFLICT);
        }
        return tokens;
    }

    public void logout(String value, String csrfToken) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        WebRefreshToken refreshToken = WebRefreshToken.parse(value);
        WebAuthSession session = sessionRepository.findById(refreshToken.sessionId()).orElse(null);
        if (session == null) {
            return;
        }
        session.requireRefreshToken(refreshToken);
        csrfTokenProvider.validate(session, csrfToken);
        if (!sessionRepository.delete(session)) {
            throw CustomException.of(ApiResponseCode.WEB_AUTH_SESSION_CONFLICT);
        }
    }

    public WebCsrfToken getCsrfToken(String value) {
        WebRefreshToken refreshToken = WebRefreshToken.parse(value);
        WebAuthSession session = getSession(refreshToken.sessionId());
        session.requireRefreshToken(refreshToken);
        return new WebCsrfToken(session.csrfToken(), session.expiresAt(), session.autoLogin());
    }

    /**
     * 세션 상태를 조회한다. 인증되지 않은 상태도 오류가 아닌 정상 결과로 돌려준다. 토큰은 회전하지 않는다.
     *
     * <p>refresh 쿠키의 세션을 우선 보고, 일치하지 않으면 access 쿠키로 확인한다. refresh만 남은 상태(access 만료)도
     * 세션이 유효하므로 인증된 것으로 응답한다. access는 이후 API 요청의 401에서 재발급된다.
     * 세션이 저장소에서 확정적으로 사라졌거나 쿠키가 형식에 맞지 않을 때만 쿠키를 지우게 한다.
     * refresh 값이 세션과 어긋나는 경우는 다른 탭이 이미 회전시켰을 수 있어 지우지 않는다.
     */
    @Transactional(readOnly = true)
    public WebSessionResult getWebSession(String refreshValue, String accessValue) {
        boolean hasRefresh = StringUtils.hasText(refreshValue);
        boolean hasAccess = StringUtils.hasText(accessValue);
        if (!hasRefresh && !hasAccess) {
            return WebSessionResult.anonymous(false);
        }

        boolean sessionGone = !hasRefresh;
        WebAuthSession session = null;
        if (hasRefresh) {
            try {
                WebRefreshToken refreshToken = WebRefreshToken.parse(refreshValue);
                Optional<WebAuthSession> found = sessionRepository.findById(refreshToken.sessionId());
                if (found.isEmpty() || !found.get().expiresAt().isAfter(Instant.now())) {
                    sessionGone = true;
                } else if (found.get().matchesRefreshToken(refreshToken)) {
                    session = found.get();
                }
            } catch (AuthenticationException e) {
                sessionGone = true;
            }
        }
        if (session == null && hasAccess) {
            try {
                session = authenticate(accessValue);
            } catch (AuthenticationException e) {
                // access가 유효하지 않으면 refresh 판단만으로 결과를 정한다.
            }
        }
        if (session == null) {
            return WebSessionResult.anonymous(sessionGone);
        }

        User user = userRepository.findById(session.userId()).orElse(null);
        if (user == null) {
            return WebSessionResult.anonymous(true);
        }
        if (!session.credentialHash().equals(WebRefreshToken.hash(user.getLoginPw()))) {
            // 비밀번호가 바뀐 세션은 refresh와 같은 기준으로 폐기한다.
            sessionRepository.delete(session);
            return WebSessionResult.anonymous(true);
        }
        return WebSessionResult.authenticated(user.getUserType().getValue(),
            new WebCsrfToken(session.csrfToken(), session.expiresAt(), session.autoLogin()));
    }

    public WebAuthSession authenticate(String accessToken) {
        JwtProvider.WebTokenClaims claims = jwtProvider.getWebTokenClaims(accessToken);
        WebAuthSession session = getSession(claims.sessionId());
        if (!session.userId().equals(claims.userId())) {
            throw AuthenticationException.withDetail("웹 로그인 사용자 정보가 일치하지 않습니다.");
        }
        // @UserId만 사용하는 API에서도 탈퇴한 계정의 쿠키를 인증하지 않는다.
        if (!userRepository.existsById(session.userId())) {
            throw AuthenticationException.withDetail("웹 로그인 사용자가 존재하지 않습니다.");
        }
        return session;
    }

    private WebAuthSession getSession(String sessionId) {
        WebAuthSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> AuthenticationException.withDetail(
                "웹 로그인 정보가 만료되었거나 로그아웃되었습니다."));
        session.requireNotExpired();
        return session;
    }

    private WebAuthTokens createTokens(User user, WebAuthSession session, WebRefreshToken refreshToken) {
        Instant accessExpiresAt = Instant.now().plus(properties.accessTokenTtl()).truncatedTo(ChronoUnit.SECONDS);
        if (accessExpiresAt.isAfter(session.expiresAt())) {
            accessExpiresAt = session.expiresAt();
        }
        return new WebAuthTokens(
            jwtProvider.createWebToken(user, session.id(), accessExpiresAt), refreshToken.value(),
            accessExpiresAt, session.expiresAt(), session.autoLogin(),
            new WebAuthResponse(user.getUserType().getValue(), session.csrfToken())
        );
    }
}
