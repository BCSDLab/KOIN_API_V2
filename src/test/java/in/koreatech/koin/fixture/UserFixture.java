package in.koreatech.koin.fixture;

import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.owner.model.Owner;
import in.koreatech.koin.domain.owner.model.OwnerAttachment;
import in.koreatech.koin.domain.owner.repository.OwnerRepository;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;

@Component
@SuppressWarnings("NonAsciiCharacters")
public final class UserFixture {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final StudentRepository studentRepository;
    private final JwtProvider jwtProvider;

    @Autowired
    public UserFixture(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository,
        OwnerRepository ownerRepository,
        StudentRepository studentRepository,
        JwtProvider jwtProvider
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.studentRepository = studentRepository;
        this.jwtProvider = jwtProvider;
    }

    public Student 준호_학생() {
        return studentRepository.save(
            Student.builder()
                .studentNumber("202020136070")
                .anonymousNickname("익명")
                .department("컴퓨터공학부")
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(
                    User.builder()
                        .password(passwordEncoder.encode("1234"))
                        .nickname("준호")
                        .name("테스트용_준호")
                        .phoneNumber("010-1234-5678")
                        .userType(STUDENT)
                        .gender(UserGender.MAN)
                        .email("juno@koreatech.ac.kr")
                        .isAuthed(true)
                        .isDeleted(false)
                        .build()
                )
                .build()
        );
    }

    public Owner 현수_사장님() {
        User user = userRepository.save(User.builder()
            .password(passwordEncoder.encode("1234"))
            .nickname("현수")
            .name("테스트용_현수")
            .phoneNumber("010-9876-5432")
            .userType(OWNER)
            .gender(UserGender.MAN)
            .email("hysoo@naver.com")
            .isAuthed(true)
            .isDeleted(false)
            .build()
        );
        return ownerRepository.save(
            Owner.builder()
                .companyRegistrationNumber("123-45-67190")
                .attachments(List.of(
                        OwnerAttachment.builder()
                            .url("https://test.com/현수_사장님_인증사진_1.jpg")
                            .isDeleted(false)
                            .build(),
                        OwnerAttachment.builder()
                            .url("https://test.com/현수_사장님_인증사진_2.jpg")
                            .isDeleted(false)
                            .build()
                    )
                )
                .grantShop(true)
                .grantEvent(true)
                .user(user)
                .build()
        );
    }

    public String getToken(User user) {
        return jwtProvider.createToken(user);
    }

    public UserFixtureBuilder builder() {
        return new UserFixtureBuilder();
    }

    public final class UserFixtureBuilder {

        private String password;
        private String nickname;
        private String name;
        private String phoneNumber;
        private UserType userType;
        private String email;
        private UserGender gender;
        private boolean isAuthed;
        private LocalDateTime lastLoggedAt;
        private String profileImageUrl;
        private Boolean isDeleted;
        private String authToken;
        private LocalDateTime authExpiredAt;
        private String resetToken;
        private LocalDateTime resetExpiredAt;
        private String deviceToken;

        public UserFixtureBuilder password(String password) {
            this.password = passwordEncoder.encode(password);
            return this;
        }

        public UserFixtureBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserFixtureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserFixtureBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserFixtureBuilder userType(UserType userType) {
            this.userType = userType;
            return this;
        }

        public UserFixtureBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserFixtureBuilder gender(UserGender gender) {
            this.gender = gender;
            return this;
        }

        public UserFixtureBuilder isAuthed(boolean isAuthed) {
            this.isAuthed = isAuthed;
            return this;
        }

        public UserFixtureBuilder lastLoggedAt(LocalDateTime lastLoggedAt) {
            this.lastLoggedAt = lastLoggedAt;
            return this;
        }

        public UserFixtureBuilder profileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public UserFixtureBuilder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public UserFixtureBuilder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public UserFixtureBuilder authExpiredAt(LocalDateTime authExpiredAt) {
            this.authExpiredAt = authExpiredAt;
            return this;
        }

        public UserFixtureBuilder resetToken(String resetToken) {
            this.resetToken = resetToken;
            return this;
        }

        public UserFixtureBuilder resetExpiredAt(LocalDateTime resetExpiredAt) {
            this.resetExpiredAt = resetExpiredAt;
            return this;
        }

        public UserFixtureBuilder deviceToken(String deviceToken) {
            this.deviceToken = deviceToken;
            return this;
        }

        public User build() {
            return userRepository.save(
                User.builder()
                    .phoneNumber(phoneNumber)
                    .authExpiredAt(authExpiredAt)
                    .deviceToken(deviceToken)
                    .lastLoggedAt(lastLoggedAt)
                    .isAuthed(isAuthed)
                    .resetExpiredAt(resetExpiredAt)
                    .resetToken(resetToken)
                    .nickname(nickname)
                    .authToken(authToken)
                    .isDeleted(isDeleted)
                    .email(email)
                    .profileImageUrl(profileImageUrl)
                    .gender(gender)
                    .password(password)
                    .userType(userType)
                    .name(name)
                    .build()
            );
        }
    }
}
