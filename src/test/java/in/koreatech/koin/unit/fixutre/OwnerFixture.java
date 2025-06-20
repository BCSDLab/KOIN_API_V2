package in.koreatech.koin.unit.fixutre;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import java.util.ArrayList;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.user.model.User;

public class OwnerFixture { // TODO : 메서드명 명확하게 변경 필요 (ex.미인증_사장님())

    private OwnerFixture() {}

    public static Owner 성빈_사장님() {
        User user = User.builder()
            .name("박성빈")
            .nickname("성빈")
            .phoneNumber("01098765439")
            .email("testsungbeenowner@naver.com")
            .loginId("test_id")
            .loginPw("test_pw")
            .userType(OWNER)
            .gender(MAN)
            .isAuthed(true)
            .isDeleted(false)
            .build();

        Owner owner = Owner.builder()
            .account("01098765439")
            .user(user)
            .companyRegistrationNumber("723-45-67190")
            .grantShop(true)
            .grantEvent(true)
            .attachments(new ArrayList<>())
            .build();

        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/성빈_사장님_인증사진_8.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/성빈_사장님_인증사진_9.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        return owner;
    }

    public static Owner 현수_사장님() {
        User user = User.builder()
            .name("테스트용_현수")
            .nickname("현수")
            .phoneNumber("01098765432")
            .email("hysoo@naver.com")
            .loginId("test_id")
            .loginPw("test_pw")
            .userType(OWNER)
            .gender(MAN)
            .isAuthed(true)
            .isDeleted(false)
            .build();

        Owner owner = Owner.builder()
            .account("01098987979")
            .user(user)
            .companyRegistrationNumber("123-45-67190")
            .grantShop(true)
            .grantEvent(true)
            .attachments(new ArrayList<>())
            .build();

        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/현수_사장님_인증사진_1.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/현수_사장님_인증사진_2.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        return owner;
    }

    public static Owner 준영_사장님() {
        User user = User.builder()
            .name("테스트용_준영")
            .nickname("준영")
            .phoneNumber("01097765112")
            .email("testjoonyoung@gmail.com")
            .loginId("test_id")
            .loginPw("test_pw")
            .userType(OWNER)
            .gender(MAN)
            .isAuthed(true)
            .isDeleted(false)
            .build();

        Owner owner = Owner.builder()
            .user(user)
            .companyRegistrationNumber("112-80-56789")
            .grantShop(true)
            .grantEvent(true)
            .account("01097765112")
            .attachments(new ArrayList<>())
            .build();

        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/준영_사장님_인증사진_1.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/준영_사장님_인증사진_2.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        return owner;
    }

    public static Owner 미인증_철수_사장님() {
        User user = User.builder()
            .name("테스트용_철수(인증X)")
            .nickname("철수")
            .phoneNumber("01097765112")
            .email("testchulsu@gmail.com")
            .loginId("test_id")
            .loginPw("test_pw")
            .userType(OWNER)
            .gender(MAN)
            .isAuthed(false)
            .isDeleted(false)
            .build();

        Owner owner = Owner.builder()
            .user(user)
            .companyRegistrationNumber("118-80-56789")
            .grantShop(false)
            .grantEvent(false)
            .account("01097765112")
            .attachments(new ArrayList<>())
            .build();

        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/철수_사장님_인증사진_1.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/철수_사장님_인증사진_2.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        return owner;
    }

    public static Owner 원경_사장님() {
        User user = User.builder()
            .name("테스트용_원경(전화번호 - 없음)")
            .nickname("원경")
            .phoneNumber("01024607469")
            .email("wongyeong@naver.com")
            .loginId("test_id")
            .loginPw("test_pw")
            .userType(OWNER)
            .gender(MAN)
            .isAuthed(true)
            .isDeleted(false)
            .build();

        Owner owner = Owner.builder()
            .user(user)
            .companyRegistrationNumber("123-45-67890")
            .grantShop(true)
            .grantEvent(true)
            .account("01024607469")
            .attachments(new ArrayList<>())
            .build();

        owner.getAttachments().add(
            OwnerAttachment.builder()
                .url("https://test.com/원경_사장님_인증사진_1.jpg")
                .isDeleted(false)
                .owner(owner)
                .build()
        );
        return owner;
    }
}
