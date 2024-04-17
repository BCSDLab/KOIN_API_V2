package in.koreatech.koin.fixture;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;

@Component
public final class UserFixture {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private final UserRepository userRepository;

    public UserFixture(UserRepository userRepository) {
        this.userRepository = userRepository;
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
