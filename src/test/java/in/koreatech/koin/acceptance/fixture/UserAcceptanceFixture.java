package in.koreatech.koin.acceptance.fixture;

import static in.koreatech.koin.admin.manager.enums.TeamType.*;
import static in.koreatech.koin.admin.manager.enums.TrackType.BACKEND;
import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserGender.WOMAN;
import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.*;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.admin.manager.repository.AdminRepository;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.coop.repository.CoopRepository;
import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class UserAcceptanceFixture {

    private final PasswordEncoder passwordEncoder;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CoopRepository coopRepository;
    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;

    @Autowired
    public UserAcceptanceFixture(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        OwnerRepository ownerRepository,
        StudentRepository studentRepository,
        CoopRepository coopRepository,
        AdminRepository adminRepository,
        JwtProvider jwtProvider
    ) {
        this.passwordEncoder = passwordEncoder;
        this.ownerRepository = ownerRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.coopRepository = coopRepository;
        this.adminRepository = adminRepository;
        this.jwtProvider = jwtProvider;
    }

    public User 코인_유저() {
        return userRepository.save(User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("주노")
            .name("최준호")
            .phoneNumber("01012345678")
            .userType(STUDENT)
            .email("test@koreatech.ac.kr")
            .loginId("test")
            .isAuthed(true)
            .isDeleted(false)
            .build()
        );
    }

    public Admin 코인_운영자() {
        return adminRepository.save(
            Admin.builder()
                .email("juno@koreatech.ac.kr")
                .loginId("juno")
                .trackType(BACKEND)
                .teamType(USER)
                .canCreateAdmin(true)
                .superAdmin(true)
                .user(
                    User.builder()
                        .loginPw(passwordEncoder.encode("1234"))
                        .nickname("코인운영자")
                        .name("테스트용_코인운영자")
                        .userType(ADMIN)
                        .gender(MAN)
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Admin 영희_운영자() {
        return adminRepository.save(
            Admin.builder()
                .email("koinadmin1@koreatech.ac.kr")
                .loginId("koinadmin1")
                .trackType(BACKEND)
                .teamType(BUSINESS)
                .canCreateAdmin(false)
                .superAdmin(false)
                .user(
                    User.builder()
                        .loginPw(passwordEncoder.encode("1234"))
                        .nickname("코인운영자1")
                        .name("테스트용_코인운영자")
                        .userType(ADMIN)
                        .gender(WOMAN)
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Admin 진구_운영자() {
        return adminRepository.save(
            Admin.builder()
                .email("koinadmin2@koreatech.ac.kr")
                .loginId("koinadmin2")
                .trackType(BACKEND)
                .teamType(CAMPUS)
                .canCreateAdmin(true)
                .superAdmin(false)
                .user(
                    User.builder()
                        .loginPw(passwordEncoder.encode("1234"))
                        .nickname("코인운영자2")
                        .name("테스트용_코인운영자")
                        .userType(ADMIN)
                        .gender(WOMAN)
                        .isAuthed(false)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Student 준호_학생(Department department, Major major) {
        return studentRepository.save(
            Student.builder()
                .studentNumber("2019136135")
                .department(department)
                .major(major)
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(
                    User.builder()
                        .loginPw(passwordEncoder.encode("1234"))
                        .name("테스트용_준호")
                        .nickname("준호")
                        .anonymousNickname("익명_주노")
                        .phoneNumber("01012345678")
                        .userType(STUDENT)
                        .gender(MAN)
                        .email("juno@koreatech.ac.kr")
                        .loginId("juno")
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Student 익명_학생(Department department) {
        return studentRepository.save(
            Student.builder()
                .studentNumber("2020136111")
                .department(department)
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(
                    User.builder()
                        .name("테스트용_익명")
                        .anonymousNickname("익명")
                        .phoneNumber("01011111111")
                        .userType(STUDENT)
                        .gender(MAN)
                        .email("lyw4888@koreatech.ac.kr")
                        .loginId("lyw4888")
                        .loginPw(passwordEncoder.encode("1234"))
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Student 성빈_학생(Department department) {
        return studentRepository.save(
            Student.builder()
                .studentNumber("2023100514")
                .department(department)
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(
                    User.builder()
                        .loginPw(passwordEncoder.encode("1234"))
                        .nickname("빈")
                        .name("박성빈")
                        .anonymousNickname("익명")
                        .phoneNumber("01099411123")
                        .userType(STUDENT)
                        .gender(MAN)
                        .email("testsungbeen@koreatech.ac.kr")
                        .loginId("testsungbeen")
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Owner 성빈_사장님() {
        User user = User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("성빈")
            .name("박성빈")
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

        OwnerAttachment attachment1 = OwnerAttachment.builder()
            .url("https://test.com/성빈_사장님_인증사진_8.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        OwnerAttachment attachment2 = OwnerAttachment.builder()
            .url("https://test.com/성빈_사장님_인증사진_9.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        owner.getAttachments().add(attachment1);
        owner.getAttachments().add(attachment2);

        return ownerRepository.save(owner);
    }

    public Owner 현수_사장님() {
        User user = User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("현수")
            .name("테스트용_현수")
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
            .account("01098765432")
            .attachments(new ArrayList<>())
            .build();

        OwnerAttachment attachment1 = OwnerAttachment.builder()
            .url("https://test.com/현수_사장님_인증사진_1.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        OwnerAttachment attachment2 = OwnerAttachment.builder()
            .url("https://test.com/현수_사장님_인증사진_2.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        owner.getAttachments().add(attachment1);
        owner.getAttachments().add(attachment2);

        return ownerRepository.save(owner);
    }

    public Owner 준영_사장님() {
        User user = User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("준영")
            .name("테스트용_준영")
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

        OwnerAttachment attachment1 = OwnerAttachment.builder()
            .url("https://test.com/준영_사장님_인증사진_1.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        OwnerAttachment attachment2 = OwnerAttachment.builder()
            .url("https://test.com/준영_사장님_인증사진_2.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        owner.getAttachments().add(attachment1);
        owner.getAttachments().add(attachment2);

        return ownerRepository.save(owner);

    }

    public Owner 철수_사장님() {
        User user = User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("철수")
            .name("테스트용_철수(인증X)")
            .phoneNumber("01097765112")
            .userType(OWNER)
            .gender(MAN)
            .email("testchulsu@gmail.com")
            .loginId("testchulsu")
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

        OwnerAttachment attachment1 = OwnerAttachment.builder()
            .url("https://test.com/철수_사장님_인증사진_1.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        OwnerAttachment attachment2 = OwnerAttachment.builder()
            .url("https://test.com/철수_사장님_인증사진_2.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        owner.getAttachments().add(attachment1);
        owner.getAttachments().add(attachment2);

        return ownerRepository.save(owner);
    }

    public Owner 원경_사장님() {
        User user = User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("원경")
            .name("테스트용_원경(전화번호 - 없음")
            .phoneNumber("01024607469")
            .userType(OWNER)
            .gender(MAN)
            .email("wongyeong@naver.com")
            .loginId("wongyeong")
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

        OwnerAttachment attachment1 = OwnerAttachment.builder()
            .url("https://test.com/원경_사장님_인증사진_1.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        OwnerAttachment attachment2 = OwnerAttachment.builder()
            .url("https://test.com/원경_사장님_인증사진_2.jpg")
            .isDeleted(false)
            .owner(owner)
            .build();

        owner.getAttachments().add(attachment1);
        owner.getAttachments().add(attachment2);

        return ownerRepository.save(owner);
    }

    public Coop 준기_영양사() {
        User user = User.builder()
            .loginPw(passwordEncoder.encode("1234"))
            .nickname("준기")
            .name("허준기")
            .phoneNumber("01011225678")
            .userType(COOP)
            .gender(MAN)
            .email("coop@koreatech.ac.kr")
            .loginId("coop")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        Coop coop = Coop.builder()
            .user(user)
            .loginId("coop")
            .build();

        return coopRepository.save(coop);
    }

    public String getToken(User user) {
        return jwtProvider.createToken(user);
    }

    public String 맥북userAgent헤더() {
        return "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/123.45 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/123.45, sec-fetch-dest=empty}";
    }
}
