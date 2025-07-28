package in.koreatech.koin.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "users")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = {"loginPw", "profileImageUrl"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 50)
    @Column(name = "nickname", unique = true, length = 50)
    private String nickname;

    @Size(max = 20)
    @Column(name = "anonymous_nickname", unique = true, length = 20)
    private String anonymousNickname;

    @Size(max = 20)
    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Size(max = 100)
    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Size(max = 30)
    @Column(name = "user_id", unique = true, length = 30)
    private String loginId;

    @NotNull
    @Column(name = "password", nullable = false)
    private String loginPw;

    @Column(name = "device_token")
    private String deviceToken;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "gender", columnDefinition = "INT")
    @Enumerated(value = EnumType.ORDINAL)
    private UserGender gender;

    @Column(name = "is_authed", nullable = false)
    private boolean isAuthed = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "last_logged_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLoggedAt;

    @Builder
    private User(
        String name,
        String nickname,
        String anonymousNickname,
        String phoneNumber,
        UserType userType,
        String email,
        String loginId,
        String loginPw,
        UserGender gender,
        boolean isAuthed,
        LocalDateTime lastLoggedAt,
        String profileImageUrl,
        Boolean isDeleted,
        String deviceToken
    ) {
        this.name = name;
        this.nickname = nickname;
        this.anonymousNickname = Objects.requireNonNullElse(anonymousNickname, RandomStringUtils.randomAlphabetic(13));
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.email = email;
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.gender = gender;
        this.isAuthed = isAuthed;
        this.lastLoggedAt = lastLoggedAt;
        this.profileImageUrl = profileImageUrl;
        this.isDeleted = isDeleted;
        this.deviceToken = deviceToken;
    }

    public void update(
        String email,
        String nickname,
        String name,
        String phoneNumber,
        UserGender gender
    ) {
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.gender = gender;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateLastLoggedTime(LocalDateTime lastLoggedTime) {
        this.lastLoggedAt = lastLoggedTime;
    }

    public void updatePassword(PasswordEncoder passwordEncoder, String loginPw) {
        if (StringUtils.hasText(loginPw)) {
            this.loginPw = passwordEncoder.encode(loginPw.replaceAll("-", ""));
        }
    }

    public boolean isNotSameNickname(String nickname) {
        return StringUtils.hasText(nickname) && !Objects.equals(this.nickname, nickname);
    }

    public boolean isNotSamePhoneNumber(String phoneNumber) {
        return StringUtils.hasText(phoneNumber) && !Objects.equals(this.phoneNumber, phoneNumber);
    }

    public boolean isNotSameEmail(String email) {
        return StringUtils.hasText(email) && !Objects.equals(this.email, email);
    }

    public boolean isNotSameLoginPw(PasswordEncoder passwordEncoder, String loginPw) {
        return StringUtils.hasText(loginPw) && !passwordEncoder.matches(loginPw, this.loginPw);
    }

    public void requireSamePhoneNumber(String phoneNumber) {
        if (isNotSamePhoneNumber(phoneNumber)) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_PHONE_NUMBER, "phoneNumber: " + phoneNumber);
        }
    }

    public void requireSameEmail(String email) {
        if (isNotSameEmail(email)) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_EMAIL, "email: " + email);
        }
    }

    public void requireSameLoginPw(PasswordEncoder passwordEncoder, String loginPw) {
        if (isNotSameLoginPw(passwordEncoder, loginPw)) {
            throw CustomException.of(ApiResponseCode.NOT_MATCHED_PASSWORD, "loginPw: " + loginPw);
        }
    }

    public void permitAuth() {
        this.isAuthed = true;
    }

    public void permitNotification(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void rejectNotification() {
        this.deviceToken = null;
    }

    // 어드민 측에서 코드 삭제시 이 쪽도 삭제
    public void undelete() {
        this.isDeleted = false;
    }

    private void ensureNotDeleted() {
        if (isDeleted) {
            throw CustomException.of(ApiResponseCode.WITHDRAWN_USER, "userId: " + id);
        }
    }

    private void ensurePermitted(UserType[] permittedUserTypes) {
        List<UserType> permittedUserTypesList = Arrays.asList(permittedUserTypes);
        if (permittedUserTypesList.contains(this.userType)) {
            return;
        }
        throw CustomException.of(ApiResponseCode.FORBIDDEN_USER_TYPE, "userId: " + id);
    }

    private void ensureAuthed() {
        if (isAuthed) {
            return;
        }
        switch (this.userType) {
            case OWNER -> throw CustomException.of(ApiResponseCode.FORBIDDEN_OWNER, "userId: " + id);
            case STUDENT -> throw CustomException.of(ApiResponseCode.FORBIDDEN_STUDENT, "userId: " + id);
            case ADMIN -> throw CustomException.of(ApiResponseCode.FORBIDDEN_ADMIN, "userId: " + id);
            default -> throw CustomException.of(ApiResponseCode.FORBIDDEN_ACCOUNT, "userId: " + id);
        }
    }

    public Integer authorizeAndGetId(UserType[] permittedUserTypes) {
        ensureNotDeleted();
        ensurePermitted(permittedUserTypes);
        ensureAuthed();
        return id;
    }
}
