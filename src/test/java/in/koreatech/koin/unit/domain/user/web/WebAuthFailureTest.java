package in.koreatech.koin.unit.domain.user.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.web.dto.WebLoginRequest;
import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.domain.user.web.repository.WebAuthSessionRedisRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthenticationException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.config.WebAuthProperties;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class WebAuthFailureTest {

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
        session = new WebAuthSession(token.sessionId(), user.getId(), token.hash(), "test-csrf-token",
            WebRefreshToken.hash(user.getLoginPw()), Instant.now().plusSeconds(3600), true);
    }

    @Test
    void access_인증_중_Redis_조회가_실패하면_사용자_조회로_진행하지_않는다() {
        RedisConnectionFailureException failure = redisFailure();
        when(sessionRepository.findById(session.id())).thenThrow(failure);

        assertThatThrownBy(() -> service.authenticate(accessToken())).isSameAs(failure);

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void 재발급_중_Redis_조회가_실패하면_사용자_조회나_회전을_진행하지_않는다() {
        RedisConnectionFailureException failure = redisFailure();
        when(sessionRepository.findById(session.id())).thenThrow(failure);

        assertThatThrownBy(() -> service.refresh(token.value(), session.csrfToken())).isSameAs(failure);

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void csrf_조회_중_Redis_조회가_실패하면_토큰을_반환하지_않는다() {
        RedisConnectionFailureException failure = redisFailure();
        when(sessionRepository.findById(session.id())).thenThrow(failure);

        assertThatThrownBy(() -> service.getCsrfToken(token.value())).isSameAs(failure);

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void 로그아웃_중_Redis_조회가_실패하면_성공으로_처리하지_않는다() {
        RedisConnectionFailureException failure = redisFailure();
        when(sessionRepository.findById(session.id())).thenThrow(failure);

        assertThatThrownBy(() -> service.logout(token.value(), session.csrfToken())).isSameAs(failure);

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void 로그인_중_Redis_저장이_실패하면_인증_결과를_반환하지_않는다() {
        WebLoginRequest request = new WebLoginRequest("test_id2", "test_pw2", true);
        RedisConnectionFailureException failure = redisFailure();
        when(userService.authenticate(request.toLoginRequest())).thenReturn(user);
        doThrow(failure).when(sessionRepository).save(any());

        assertThatThrownBy(() -> service.login(request)).isSameAs(failure);

        verify(sessionRepository).save(any());
        verify(sessionRepository, never()).rotate(any(), any());
        verify(sessionRepository, never()).delete(any());
        verifyNoInteractions(userRepository);
    }

    @Test
    void 재발급_중_Redis_회전이_실패하면_새_토큰을_반환하거나_세션을_삭제하지_않는다() {
        RedisConnectionFailureException failure = redisFailure();
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(sessionRepository.rotate(any(), any())).thenThrow(failure);

        assertThatThrownBy(() -> service.refresh(token.value(), session.csrfToken())).isSameAs(failure);

        verify(sessionRepository).rotate(any(), any());
        verify(sessionRepository, never()).save(any());
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void 로그아웃_중_Redis_삭제가_실패하면_성공으로_처리하지_않는다() {
        RedisConnectionFailureException failure = redisFailure();
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(sessionRepository.delete(session)).thenThrow(failure);

        assertThatThrownBy(() -> service.logout(token.value(), session.csrfToken())).isSameAs(failure);

        verify(sessionRepository).delete(session);
        verify(sessionRepository, never()).save(any());
        verify(sessionRepository, never()).rotate(any(), any());
        verifyNoInteractions(userRepository, userService);
    }

    @Test
    void 유효한_JWT와_Redis_세션이_있어도_탈퇴한_사용자는_인증하지_않는다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.existsById(user.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(accessToken())).isInstanceOf(AuthenticationException.class);

        verify(userRepository).existsById(user.getId());
        verify(sessionRepository, never()).save(any());
        verify(sessionRepository, never()).rotate(any(), any());
    }

    @Test
    void 유효한_JWT와_Redis_세션이_있고_사용자가_존재하면_인증한다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.existsById(user.getId())).thenReturn(true);

        assertThat(service.authenticate(accessToken())).isEqualTo(session);

        verify(userRepository).existsById(user.getId());
        verifyNoSessionMutation();
    }

    @Test
    void csrf가_틀린_로그아웃은_세션을_삭제하지_않는다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.logout(token.value(), "different-test-csrf-token"))
            .isInstanceOf(CustomException.class).hasMessage(ApiResponseCode.INVALID_CSRF_TOKEN.getMessage());

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void 만료된_세션은_refresh와_csrf가_일치해도_재발급하지_않는다() {
        WebAuthSession expired = expiredSession();
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.refresh(token.value(), expired.csrfToken()))
            .isInstanceOf(AuthenticationException.class);

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void 만료된_세션으로는_csrf_토큰을_조회할_수_없다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(expiredSession()));

        assertThatThrownBy(() -> service.getCsrfToken(token.value())).isInstanceOf(AuthenticationException.class);

        verifyNoInteractions(userRepository, userService);
        verifyNoSessionMutation();
    }

    @Test
    void 재발급_시_사용자가_없으면_세션을_회전하거나_다시_저장하지_않는다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(token.value(), session.csrfToken()))
            .isInstanceOf(AuthenticationException.class);

        verify(sessionRepository, never()).save(any());
        verify(sessionRepository, never()).rotate(any(), any());
    }

    private String accessToken() {
        return jwtProvider.createWebToken(user, session.id(), Instant.now().plusSeconds(600));
    }

    private WebAuthSession expiredSession() {
        return new WebAuthSession(session.id(), session.userId(), token.hash(), session.csrfToken(),
            session.credentialHash(), Instant.now().minusSeconds(1), session.autoLogin());
    }

    private RedisConnectionFailureException redisFailure() {
        return new RedisConnectionFailureException("테스트용 Redis 연결 실패");
    }

    private void verifyNoSessionMutation() {
        verify(sessionRepository, never()).save(any());
        verify(sessionRepository, never()).rotate(any(), any());
        verify(sessionRepository, never()).delete(any());
    }
}
