package in.koreatech.koin.unit.domain.user.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.web.dto.WebLoginRequest;
import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.domain.user.web.repository.WebAuthSessionRedisRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.domain.user.web.service.WebAuthTokens;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.WebAuthProperties;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class WebAuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebAuthSessionRedisRepository sessionRepository;

    private final User user = UserFixture.id_설정_코인_유저(1);
    private final JwtProvider jwtProvider = new JwtProvider("web-auth-unit-test-key-32-characters", 600_000L);
    private WebAuthService service;
    private WebRefreshToken token;
    private WebAuthSession session;

    @BeforeEach
    void setUp() {
        service = new WebAuthService(userService, userRepository, sessionRepository, jwtProvider,
            new WebAuthProperties(Duration.ofMinutes(15), Duration.ofDays(90), true, "Lax"));
        token = WebRefreshToken.create();
        session = new WebAuthSession(token.sessionId(), 1, token.hash(), "csrf-token",
            WebRefreshToken.hash(user.getLoginPw()), Instant.now().plusSeconds(3600), true);
    }

    @Test
    void 로그인마다_독립된_웹_세션을_발급하고_refresh는_해시만_저장한다() {
        WebLoginRequest request = new WebLoginRequest("test_id2", "test_pw2", true);
        when(userService.authenticate(request.toLoginRequest())).thenReturn(user);

        WebAuthTokens first = service.login(request);
        WebAuthTokens second = service.login(request);

        assertThat(WebRefreshToken.parse(first.refreshToken()).sessionId())
            .isNotEqualTo(WebRefreshToken.parse(second.refreshToken()).sessionId());
        ArgumentCaptor<WebAuthSession> captor = ArgumentCaptor.forClass(WebAuthSession.class);
        verify(sessionRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).refreshTokenHash()).isEqualTo(WebRefreshToken.parse(first.refreshToken()).hash());
        assertThat(captor.getAllValues().get(0).refreshTokenHash()).doesNotContain(first.refreshToken());
    }

    @Test
    void 로그인_실패시_웹_세션을_저장하지_않는다() {
        when(userService.authenticate(any())).thenThrow(CustomException.of(ApiResponseCode.NOT_MATCHED_PASSWORD));

        assertThatThrownBy(() -> service.login(new WebLoginRequest("id", "wrong", false)))
            .isInstanceOf(CustomException.class);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void 재발급은_토큰만_교체하고_세션과_자동로그인_기한을_유지한다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(sessionRepository.rotate(any(), any())).thenReturn(true);

        WebAuthTokens refreshed = service.refresh(token.value(), session.csrfToken());

        assertThat(refreshed.refreshToken()).isNotEqualTo(token.value());
        assertThat(WebRefreshToken.parse(refreshed.refreshToken()).sessionId()).isEqualTo(session.id());
        assertThat(refreshed.refreshExpiresAt()).isEqualTo(session.expiresAt());
        assertThat(refreshed.response().csrfToken()).isEqualTo(session.csrfToken());
        assertThat(refreshed.autoLogin()).isTrue();
    }

    @Test
    void 다른_요청이_먼저_갱신한_세션을_덮어쓰지_않는다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(sessionRepository.rotate(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.refresh(token.value(), session.csrfToken()))
            .isInstanceOf(CustomException.class).hasMessage(ApiResponseCode.WEB_AUTH_SESSION_CONFLICT.getMessage());
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void 비밀번호가_변경되면_기존_웹_세션으로_재발급하지_않는다() {
        WebAuthSession previousPasswordSession = new WebAuthSession(session.id(), 1, token.hash(), session.csrfToken(),
            "previous-password-hash", session.expiresAt(), true);
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(previousPasswordSession));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.refresh(token.value(), session.csrfToken())).isInstanceOf(AuthenticationException.class);
        verify(sessionRepository).delete(previousPasswordSession);
        verify(sessionRepository, never()).rotate(any(), any());
    }

    @Test
    void csrf_토큰이_틀리면_세션을_변경하지_않는다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.refresh(token.value(), "wrong"))
            .isInstanceOf(CustomException.class).hasMessage(ApiResponseCode.INVALID_CSRF_TOKEN.getMessage());
        verify(sessionRepository, never()).rotate(any(), any());
    }

    @Test
    void 만료된_세션은_access_토큰이_유효해도_거부한다() {
        WebAuthSession expired = new WebAuthSession(session.id(), 1, token.hash(), session.csrfToken(),
            session.credentialHash(), Instant.now().minusSeconds(1), true);
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(expired));
        String accessToken = jwtProvider.createWebToken(user, session.id(), Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> service.authenticate(accessToken)).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 토큰의_사용자와_세션의_사용자가_다르면_거부한다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        String accessToken = jwtProvider.createWebToken(
            UserFixture.id_설정_코인_유저(2), session.id(), Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> service.authenticate(accessToken)).isInstanceOf(AuthenticationException.class);
    }
}
