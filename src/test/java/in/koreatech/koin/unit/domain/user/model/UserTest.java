package in.koreatech.koin.unit.domain.user.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;
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

    @Nested
    class isNotSameNickname {

        @Test
        void 닉네임이_불일치하면_true를_반환한다() {
            // when
            boolean result = user.isNotSameNickname("otherNick");
            // then
            assertTrue(result);
        }

        @Test
        void 닉네임이_일치하면_false를_반환한다() {
            // when
            boolean result = user.isNotSameNickname(user.getNickname());
            // then
            assertFalse(result);
        }
    }

    @Nested
    class isNotSamePhoneNumber {

        @Test
        void 전화번호가_불일치하면_true를_반환한다() {
            // when
            boolean result = user.isNotSamePhoneNumber("01000000000");
            // then
            assertTrue(result);
        }

        @Test
        void 전화번호가_일치하면_false를_반환한다() {
            // when
            boolean result = user.isNotSamePhoneNumber(user.getPhoneNumber());
            // then
            assertFalse(result);
        }
    }

    @Nested
    class isNotSameEmail {

        @Test
        void 이메일이_불일치하면_true를_반환한다() {
            // when
            boolean result = user.isNotSameEmail("x@y.com");
            // then
            assertTrue(result);
        }

        @Test
        void 이메일이_일치하면_false를_반환한다() {
            // when
            boolean result = user.isNotSameEmail(user.getEmail());
            // then
            assertFalse(result);
        }
    }

    @Nested
    class isNotSameLoginPw {

        @Test
        void 비밀번호가_불일치하면_true를_반환한다() {
            // given
            when(passwordEncoder.matches("raw", user.getLoginPw())).thenReturn(false);
            // when
            boolean result = user.isNotSameLoginPw(passwordEncoder, "raw");
            // then
            assertTrue(result);
        }

        @Test
        void 비밀번호가_일치하면_false를_반환한다() {
            // given
            when(passwordEncoder.matches("raw", user.getLoginPw())).thenReturn(true);
            // when
            boolean result = user.isNotSameLoginPw(passwordEncoder, "raw");
            // then
            assertFalse(result);
        }
    }

    @Nested
    class requireSamePhoneNumber {

        @Test
        void 전화번호가_일치하는지_확인한다() {
            // given
            doReturn(false).when(user).isNotSamePhoneNumber(anyString());
            // when / then
            assertDoesNotThrow(() -> user.requireSamePhoneNumber(user.getPhoneNumber()));
            verify(user, times(1)).isNotSamePhoneNumber(anyString());
        }

        @Test
        void 전화번호가_불일치하면_예외를_던진다() {
            // given
            doReturn(true).when(user).isNotSamePhoneNumber(anyString());
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> user.requireSamePhoneNumber("01000000000"));
            assertEquals(ApiResponseCode.NOT_MATCHED_PHONE_NUMBER, exception.getErrorCode());
            verify(user, times(1)).isNotSamePhoneNumber(anyString());
        }
    }

    @Nested
    class requireSameEmail {

        @Test
        void 이메일이_일치하는지_확인한다() {
            // given
            doReturn(false).when(user).isNotSameEmail(anyString());
            // when / then
            assertDoesNotThrow(() -> user.requireSameEmail(user.getEmail()));
            verify(user, times(1)).isNotSameEmail(anyString());
        }

        @Test
        void 이메일이_불일치하면_예외를_던진다() {
            // given
            doReturn(true).when(user).isNotSameEmail(anyString());
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> user.requireSameEmail("x@y.com"));
            assertEquals(ApiResponseCode.NOT_MATCHED_EMAIL, exception.getErrorCode());
            verify(user, times(1)).isNotSameEmail(anyString());
        }
    }

    @Nested
    class requireSameLoginPw {

        @Test
        void 비밀번호가_일치하는지_확인한다() {
            // given
            doReturn(false).when(user).isNotSameLoginPw(passwordEncoder, "raw");
            // when / then
            assertDoesNotThrow(() -> user.requireSameLoginPw(passwordEncoder, "raw"));
            verify(user, times(1)).isNotSameLoginPw(any(), anyString());
        }

        @Test
        void 비밀번호가_불일치하면_예외를_던진다() {
            // given
            doReturn(true).when(user).isNotSameLoginPw(passwordEncoder, "raw");
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> user.requireSameLoginPw(passwordEncoder, "raw"));
            assertEquals(ApiResponseCode.NOT_MATCHED_PASSWORD, exception.getErrorCode());
            verify(user, times(1)).isNotSameLoginPw(any(), anyString());
        }
    }

    @Nested
    class updateName {

        @Test
        void 이름을_업데이트한다() {
            // given
            String newName = "새이름";
            // when
            user.updateName(newName);
            // then
            assertEquals(newName, user.getName());
        }
    }

    @Nested
    class updateLastLoggedTime {

        @Test
        void 마지막_로그인_시간을_업데이트한다() {
            // given
            LocalDateTime before = LocalDateTime.now().minusDays(1);
            // when
            user.updateLastLoggedTime(before);
            // then
            assertEquals(before, user.getLastLoggedAt());
        }
    }

    @Nested
    class updatePassword {

        @Test
        void 빈_비밀번호로_업데이트하면_유지된다() {
            // given
            String original = user.getLoginPw();
            // when
            user.updatePassword(passwordEncoder, "");
            // then
            assertEquals(original, user.getLoginPw());
        }

        @Test
        void 새_비밀번호가_암호화되어_저장된다() {
            // given
            when(passwordEncoder.encode("비밀번호")).thenReturn("암호화된_비밀번호");
            // when
            user.updatePassword(passwordEncoder, "비밀번호");
            // then
            assertEquals("암호화된_비밀번호", user.getLoginPw());
        }
    }

    @Nested
    class permitAuth {

        @Test
        void 인증상태를_허용한다() {
            // given
            user = UserFixture.미인증_코인_유저();
            // when
            user.permitAuth();
            // then
            assertTrue(user.isAuthed());
        }
    }

    @Nested
    class permitNotification {

        @Test
        void 디바이스토큰을_설정한다() {
            // when
            user.permitNotification("token");
            // then
            assertEquals("token", user.getDeviceToken());
        }

        @Test
        void 디바이스토큰을_제거한다() {
            // when
            user.rejectNotification();
            // then
            assertNull(user.getDeviceToken());
        }
    }

    @Nested
    class undelete {

        @Test
        void 삭제된_유저를_복구한다() {
            // given
            user = UserFixture.삭제된_코인_유저();
            // when
            user.undelete();
            // then
            assertFalse(user.isDeleted());
        }
    }

    @Nested
    class authorizeAndGetId {

        @Test
        void 정상_유저타입이면_id를_반환한다() {
            // when / then
            Integer id = user.authorizeAndGetId(new UserType[] {UserType.GENERAL, UserType.STUDENT});
            assertEquals(user.getId(), id);
        }

        @Test
        void 삭제된_계정이면_예외를_던진다() {
            // given
            user = UserFixture.삭제된_코인_유저();
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> user.authorizeAndGetId(new UserType[] {UserType.GENERAL}));
            assertEquals(ApiResponseCode.WITHDRAWN_USER, exception.getErrorCode());

        }

        @Test
        void 권한이_없으면_예외를_던진다() {
            // given
            user = UserFixture.코인_유저();
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> user.authorizeAndGetId(new UserType[] {UserType.STUDENT}));
            assertEquals(ApiResponseCode.FORBIDDEN_USER_TYPE, exception.getErrorCode());
        }

        @Test
        void 미인증_상태면_예외를_던진다() {
            // given
            user = UserFixture.미인증_코인_유저();
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> user.authorizeAndGetId(new UserType[] {UserType.GENERAL}));
            assertEquals(ApiResponseCode.FORBIDDEN_ACCOUNT, exception.getErrorCode());
        }
    }
}
