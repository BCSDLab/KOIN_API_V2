package in.koreatech.koin.domain.user.web.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.WebAuthProperties;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebAuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final WebAuthSessionRedisRepository sessionRepository;
    private final JwtProvider jwtProvider;
    private final WebAuthProperties properties;

    @Transactional
    public WebAuthTokens login(WebLoginRequest request) {
        User user = userService.authenticate(request.toLoginRequest());
        WebRefreshToken refreshToken = WebRefreshToken.create();
        WebAuthSession session = new WebAuthSession(
            refreshToken.sessionId(), user.getId(), refreshToken.hash(), WebRefreshToken.createSecret(),
            WebRefreshToken.hash(user.getLoginPw()),
            Instant.now().plus(properties.refreshTokenTtl()).truncatedTo(ChronoUnit.SECONDS), request.autoLogin()
        );
        WebAuthTokens tokens = createTokens(user, session, refreshToken);
        sessionRepository.save(session);
        return tokens;
    }

    public WebAuthTokens refresh(String value, String csrfToken) {
        WebRefreshToken refreshToken = WebRefreshToken.parse(value);
        WebAuthSession session = getSession(refreshToken.sessionId());
        session.requireRefreshToken(refreshToken);
        session.requireCsrfToken(csrfToken);

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
        session.requireCsrfToken(csrfToken);
        if (!sessionRepository.delete(session)) {
            throw CustomException.of(ApiResponseCode.WEB_AUTH_SESSION_CONFLICT);
        }
    }

    public String getCsrfToken(String value) {
        WebRefreshToken refreshToken = WebRefreshToken.parse(value);
        WebAuthSession session = getSession(refreshToken.sessionId());
        session.requireRefreshToken(refreshToken);
        return session.csrfToken();
    }

    public WebAuthSession authenticate(String accessToken) {
        JwtProvider.WebTokenClaims claims = jwtProvider.getWebTokenClaims(accessToken);
        WebAuthSession session = getSession(claims.sessionId());
        if (!session.userId().equals(claims.userId())) {
            throw AuthenticationException.withDetail("웹 로그인 사용자 정보가 일치하지 않습니다.");
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
