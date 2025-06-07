package in.koreatech.koin.unit.domain.user.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.unit.fixutre.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private User user;

    @BeforeEach
    void init() {
        user = spy(UserFixture.코인_유저());
    }

    @Test
    void isNotSameNickname_닉네임이_불일치하면_true를_반환한다() {
        // when
        boolean result = user.isNotSameNickname("otherNick");
        // then
        assertTrue(result);
    }

    @Test
    void isNotSameNickname_닉네임이_일치하면_false를_반환한다() {
        // when
        boolean result = user.isNotSameNickname(user.getNickname());
        // then
        assertFalse(result);
    }

    @Test
    void isNotSamePhoneNumber_전화번호가_불일치하면_true를_반환한다() {
        // when
        boolean result = user.isNotSamePhoneNumber("01000000000");
        // then
        assertTrue(result);
    }

    @Test
    void isNotSamePhoneNumber_전화번호가_일치하면_false를_반환한다() {
        // when
        boolean result = user.isNotSamePhoneNumber(user.getPhoneNumber());
        // then
        assertFalse(result);
    }

    @Test
    void isNotSameEmail_이메일이_불일치하면_true를_반환한다() {
        // when
        boolean result = user.isNotSameEmail("x@y.com");
        // then
        assertTrue(result);
    }

    @Test
    void isNotSameEmail_이메일이_일치하면_false를_반환한다() {
        // when
        boolean result = user.isNotSameEmail(user.getEmail());
        // then
        assertFalse(result);
    }

    @Test
    void isNotSameLoginPw_비밀번호가_불일치하면_true를_반환한다() {
        // given
        when(passwordEncoder.matches("raw", user.getLoginPw())).thenReturn(false);

        // when
        boolean result = user.isNotSameLoginPw(passwordEncoder, "raw");

        // then
        assertTrue(result);
    }

    @Test
    void isNotSameLoginPw_비밀번호가_일치하면_false를_반환한다() {
        // given
        when(passwordEncoder.matches("raw", user.getLoginPw())).thenReturn(true);

        // when
        boolean result = user.isNotSameLoginPw(passwordEncoder, "raw");

        // then
        assertFalse(result);
    }

    @Test
    void requireSamePhoneNumber_전화번호가_일치하는지_확인한다() {
        // given
        doReturn(false).when(user).isNotSamePhoneNumber(anyString());
        // when / then
        assertDoesNotThrow(() -> user.requireSamePhoneNumber(user.getPhoneNumber()));
        verify(user, times(1)).isNotSamePhoneNumber(anyString());
    }

    @Test
    void requireSamePhoneNumber_전화번호가_불일치하면_예외를_던진다() {
        // given
        doReturn(true).when(user).isNotSamePhoneNumber(anyString());
        // when / then
        assertThrows(KoinIllegalArgumentException.class,
            () -> user.requireSamePhoneNumber("01000000000"));
        verify(user, times(1)).isNotSamePhoneNumber(anyString());
    }

    @Test
    void requireSameEmail_이메일이_일치다하는지_확인한다() {
        // given
        doReturn(false).when(user).isNotSameEmail(anyString());
        // when / then
        assertDoesNotThrow(() -> user.requireSameEmail(user.getEmail()));
        verify(user, times(1)).isNotSameEmail(anyString());
    }

    @Test
    void requireSameEmail_이메일이_불일치하면_예외를_던진다() {
        // given
        doReturn(true).when(user).isNotSameEmail(anyString());
        // when / then
        assertThrows(KoinIllegalArgumentException.class,
            () -> user.requireSameEmail("x@y.com"));
        verify(user, times(1)).isNotSameEmail(anyString());
    }

    @Test
    void requireSameLoginPw_비밀번호가_일치하는지_확인한다() {
        // given
        doReturn(false).when(user).isNotSameLoginPw(passwordEncoder, "raw");
        // when / then
        assertDoesNotThrow(() -> user.requireSameLoginPw(passwordEncoder, "raw"));
        verify(user, times(1)).isNotSameLoginPw(any(), anyString());
    }

    @Test
    void requireSameLoginPw_비밀번호가_불일치하면_예외를_던진다() {
        // given
        doReturn(true).when(user).isNotSameLoginPw(passwordEncoder, "raw");
        // when / then
        assertThrows(KoinIllegalArgumentException.class,
            () -> user.requireSameLoginPw(passwordEncoder, "raw"));
        verify(user, times(1)).isNotSameLoginPw(any(), anyString());
    }

    @Test
    void updateName_이름을_업데이트한다() {
        // given
        String newName = "새이름";
        // when
        user.updateName(newName);
        // then
        assertEquals(newName, user.getName());
    }

    @Test
    void updateLastLoggedTime_마지막_로그인_시간을_업데이트한다() {
        // given
        LocalDateTime before = LocalDateTime.now().minusDays(1);
        // when
        user.updateLastLoggedTime(before);
        // then
        assertEquals(before, user.getLastLoggedAt());
    }

    @Test
    void updatePassword_빈_비밀번호로_업데이트하면_유지된다() {
        // given
        String original = user.getLoginPw();
        // when
        user.updatePassword(passwordEncoder, "");
        // then
        assertEquals(original, user.getLoginPw());
    }

    @Test
    void updatePassword_새_비밀번호가_암호화되어_저장된다() {
        // given
        when(passwordEncoder.encode("비밀번호")).thenReturn("암호화된_비밀번호");
        // when
        user.updatePassword(passwordEncoder, "비밀번호");
        // then
        assertEquals("암호화된_비밀번호", user.getLoginPw());
    }

    @Test
    void permitAuth_인증상태를_허용한다() {
        // given
        user = UserFixture.미인증_코인_유저();
        // when
        user.permitAuth();
        // then
        assertTrue(user.isAuthed());
    }

    @Test
    void permitNotification_디바이스토큰을_설정한다() {
        // when
        user.permitNotification("token");
        // then
        assertEquals("token", user.getDeviceToken());
    }

    @Test
    void rejectNotification_디바이스토큰을_제거한다() {
        // when
        user.rejectNotification();
        // then
        assertNull(user.getDeviceToken());
    }

    @Test
    void undelete_삭제된_유저를_복구한다() {
        // given
        user = UserFixture.삭제된_코인_유저();
        // when
        user.undelete();
        // then
        assertFalse(user.isDeleted());
    }

    @Test
    void authorizeAndGetId_정상_유저타입이면_id를_반환한다() {
        // when / then
        Integer id = user.authorizeAndGetId(new UserType[] {UserType.GENERAL, UserType.STUDENT});
        assertEquals(user.getId(), id);
    }

    @Test
    void authorizeAndGetId_삭제된_계정이면_예외를_던진다() {
        // given
        user = UserFixture.삭제된_코인_유저();
        // when / then
        assertThrows(AuthenticationException.class,
            () -> user.authorizeAndGetId(new UserType[] {UserType.GENERAL}));
    }

    @Test
    void authorizeAndGetId_권한이_없으면_예외를_던진다() {
        // given
        user = UserFixture.코인_유저();
        // when / then
        assertThrows(AuthorizationException.class,
            () -> user.authorizeAndGetId(new UserType[] {UserType.STUDENT}));
    }

    @Test
    void authorizeAndGetId_미인증_상태면_예외를_던진다() {
        // given
        user = UserFixture.미인증_코인_유저();
        // when / then
        assertThrows(AuthorizationException.class,
            () -> user.authorizeAndGetId(new UserType[] {UserType.GENERAL}));
    }
}
