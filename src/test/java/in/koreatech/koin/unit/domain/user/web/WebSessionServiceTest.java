package in.koreatech.koin.unit.domain.user.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import in.koreatech.koin.domain.user.web.model.WebAuthSession;
import in.koreatech.koin.domain.user.web.model.WebRefreshToken;
import in.koreatech.koin.domain.user.web.repository.WebAuthSessionRedisRepository;
import in.koreatech.koin.domain.user.web.service.WebAuthService;
import in.koreatech.koin.domain.user.web.service.WebSessionResult;
import in.koreatech.koin.domain.user.web.service.WebSessionResult.Status;
import in.koreatech.koin.domain.user.web.service.WebSessionService;
import in.koreatech.koin.global.auth.WebCsrfTokenProvider;
import in.koreatech.koin.unit.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class WebSessionServiceTest {

    @Mock
    private WebAuthService webAuthService;

    @Mock
    private WebAuthSessionRedisRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    private final User user = UserFixture.id_설정_코인_유저(1);
    private final WebCsrfTokenProvider csrfTokenProvider = new WebCsrfTokenProvider("csrf-test-signing-key-at-least-32-bytes");
    private WebSessionService service;
    private WebRefreshToken token;
    private WebAuthSession session;

    @BeforeEach
    void setUp() {
        service = new WebSessionService(webAuthService, sessionRepository, userRepository);
        token = WebRefreshToken.create();
        session = new WebAuthSession(token.sessionId(), user.getId(), token.hash(),
            csrfTokenProvider.createToken(token.sessionId()), WebRefreshToken.hash(user.getLoginPw()),
            Instant.now().plusSeconds(3600), true);
    }

    @Test
    void 쿠키가_없으면_저장소를_조회하지_않고_익명이다() {
        WebSessionResult result = service.getSession(null, null);

        assertThat(result.status()).isEqualTo(Status.ANONYMOUS);
        verifyNoInteractions(webAuthService, sessionRepository, userRepository);
    }

    @Test
    void 유효한_refresh_세션이면_회원_유형과_CSRF를_돌려주고_세션을_변경하지_않는다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        WebSessionResult result = service.getSession(token.value(), null);

        assertThat(result.status()).isEqualTo(Status.AUTHENTICATED);
        assertThat(result.userType()).isEqualTo(user.getUserType().getValue());
        assertThat(result.csrfToken().value()).isEqualTo(session.csrfToken());
        verifyNoSessionMutation();
    }

    @Test
    void access가_만료되어도_refresh_세션이_유효하면_인증으로_본다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        WebSessionResult result = service.getSession(token.value(), "expired-access");

        assertThat(result.status()).isEqualTo(Status.AUTHENTICATED);
        verifyNoInteractions(webAuthService);
    }

    @Test
    void refresh가_없어도_유효한_access_세션이면_인증으로_본다() {
        when(webAuthService.findSessionByAccessToken("valid-access")).thenReturn(Optional.of(session));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        WebSessionResult result = service.getSession(null, "valid-access");

        assertThat(result.status()).isEqualTo(Status.AUTHENTICATED);
    }

    @Test
    void 저장소에_세션이_없으면_세션_소멸이다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.empty());

        assertThat(service.getSession(token.value(), null).status()).isEqualTo(Status.SESSION_LOST);
        verifyNoSessionMutation();
    }

    @Test
    void 만료된_세션은_세션_소멸이다() {
        WebAuthSession expired = new WebAuthSession(session.id(), session.userId(), token.hash(), session.csrfToken(),
            session.credentialHash(), Instant.now().minusSeconds(1), session.autoLogin());
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(expired));

        assertThat(service.getSession(token.value(), null).status()).isEqualTo(Status.SESSION_LOST);
    }

    @Test
    void 형식이_잘못된_refresh_쿠키는_저장소를_조회하지_않고_세션_소멸이다() {
        WebSessionResult result = service.getSession("not-a-refresh-token", null);

        assertThat(result.status()).isEqualTo(Status.SESSION_LOST);
        verifyNoInteractions(sessionRepository);
    }

    @Test
    void 다른_탭이_회전시킨_이전_refresh는_익명으로_두고_쿠키를_버리게_하지_않는다() {
        WebAuthSession rotated = session.rotate(token.rotate());
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(rotated));

        assertThat(service.getSession(token.value(), null).status()).isEqualTo(Status.ANONYMOUS);
        verifyNoSessionMutation();
    }

    @Test
    void 회전된_refresh여도_access가_유효하면_인증으로_본다() {
        WebAuthSession rotated = session.rotate(token.rotate());
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(rotated));
        when(webAuthService.findSessionByAccessToken("valid-access")).thenReturn(Optional.of(rotated));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(service.getSession(token.value(), "valid-access").status()).isEqualTo(Status.AUTHENTICATED);
    }

    @Test
    void 탈퇴한_계정의_세션은_세션_소멸이다() {
        when(sessionRepository.findById(session.id())).thenReturn(Optional.of(session));
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThat(service.getSession(token.value(), null).status()).isEqualTo(Status.SESSION_LOST);
    }

    @Test
    void Redis_조회가_실패하면_익명으로_단정하지_않고_오류를_전달한다() {
        RedisConnectionFailureException failure = new RedisConnectionFailureException("테스트용 Redis 연결 실패");
        when(sessionRepository.findById(session.id())).thenThrow(failure);

        assertThatThrownBy(() -> service.getSession(token.value(), null)).isSameAs(failure);

        verifyNoInteractions(userRepository);
        verifyNoSessionMutation();
    }

    private void verifyNoSessionMutation() {
        verify(sessionRepository, never()).save(any());
        verify(sessionRepository, never()).rotate(any(), any());
        verify(sessionRepository, never()).delete(any());
    }
}
