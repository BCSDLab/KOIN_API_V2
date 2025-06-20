package in.koreatech.koin.unit.fixutre;

import static in.koreatech.koin.admin.user.enums.TeamType.*;
import static in.koreatech.koin.admin.user.enums.TrackType.BACKEND;
import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserGender.WOMAN;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;

public class AdminFixture { // TODO : 메서드명 명확하게 변경 필요 (ex.미인증_운영자())

    private AdminFixture() {}

    public static Admin 코인_운영자() {
        return Admin.builder()
            .trackType(BACKEND)
            .teamType(USER)
            .canCreateAdmin(true)
            .superAdmin(true)
            .user(
                User.builder()
                    .name("테스트용_코인운영자")
                    .nickname("코인운영자")
                    .phoneNumber("01012342344")
                    .email("juno@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(ADMIN)
                    .gender(MAN)
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }

    public static Admin 영희_운영자() {
        return Admin.builder()
            .trackType(BACKEND)
            .teamType(BUSINESS)
            .canCreateAdmin(false)
            .superAdmin(false)
            .user(
                User.builder()
                    .name("테스트용_코인운영자")
                    .nickname("코인운영자1")
                    .phoneNumber("01012342347")
                    .email("koinadmin1@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(ADMIN)
                    .gender(WOMAN)
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }

    public static Admin 미인증_진구_운영자() {
        return Admin.builder()
            .trackType(BACKEND)
            .teamType(CAMPUS)
            .canCreateAdmin(true)
            .superAdmin(false)
            .user(
                User.builder()
                    .name("테스트용_코인운영자")
                    .nickname("코인운영자2")
                    .phoneNumber("01012342347")
                    .email("koinadmin2@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(ADMIN)
                    .gender(WOMAN)
                    .isAuthed(false)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }
}
