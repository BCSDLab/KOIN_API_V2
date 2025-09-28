package in.koreatech.koin.unit.fixture;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserGender.WOMAN;
import static in.koreatech.koin.domain.user.model.UserType.GENERAL;

import in.koreatech.koin.domain.user.model.User;

public final class UserFixture {

    private UserFixture() {}

    public static User 코인_유저() {
        return User.builder()
            .name("최준호")
            .nickname("주노")
            .phoneNumber("01012345678")
            .email("test@koreatech.ac.kr")
            .loginId("test_id")
            .loginPw("test_pw")
            .gender(MAN)
            .userType(GENERAL)
            .isAuthed(true)
            .isDeleted(false)
            .build();
    }

    public static User 미인증_코인_유저() {
        return User.builder()
            .name("배진호")
            .nickname("진호")
            .phoneNumber("01012341234")
            .email("test@koreatech.ac.kr")
            .loginId("test_id")
            .loginPw("test_pw")
            .gender(MAN)
            .userType(GENERAL)
            .isAuthed(false)
            .isDeleted(false)
            .build();
    }

    public static User 삭제된_코인_유저() {
        return User.builder()
            .name("최준호")
            .nickname("주노")
            .phoneNumber("01012345678")
            .email("test@koreatech.ac.kr")
            .loginId("test_id")
            .loginPw("test_pw")
            .gender(WOMAN)
            .userType(GENERAL)
            .isAuthed(true)
            .isDeleted(true)
            .build();
    }

    public static User id_설정_코인_유저(Integer id) {
        return User.builder()
            .id(id)
            .name("이름")
            .nickname("닉네임")
            .phoneNumber("01012345678")
            .email("test2@koreatech.ac.kr")
            .loginId("test_id2")
            .loginPw("test_pw2")
            .gender(MAN)
            .userType(GENERAL)
            .isAuthed(true)
            .isDeleted(false)
            .build();
    }
}
