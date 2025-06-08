package in.koreatech.koin.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.model.BaseEntity;
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

@Getter
@Entity
@Table(name = "users")
@Where(clause = "is_deleted=0")
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 50)
    @Column(name = "nickname", length = 50, unique = true)
    private String nickname;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 255)
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String loginId;

    @NotNull
    @Column(name = "password", nullable = false)
    private String loginPw;

    @Column(name = "device_token")
    private String deviceToken;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Column(name = "gender", columnDefinition = "INT")
    @Enumerated(value = EnumType.ORDINAL)
    private UserGender gender;

    @NotNull
    @Column(name = "is_authed", nullable = false)
    private boolean isAuthed = false;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "last_logged_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLoggedAt;

    @Builder
    private User(
        String loginId,
        String loginPw,
        String nickname,
        String name,
        String phoneNumber,
        UserType userType,
        String email,
        UserGender gender,
        boolean isAuthed,
        LocalDateTime lastLoggedAt,
        String profileImageUrl,
        Boolean isDeleted,
        String deviceToken
    ) {
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.nickname = nickname;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
        this.email = email;
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
            throw new KoinIllegalArgumentException("전화번호가 일치하지 않습니다.");
        }
    }

    public void requireSameEmail(String email) {
        if (isNotSameEmail(email)) {
            throw new KoinIllegalArgumentException("이메일이 일치하지 않습니다.");
        }
    }

    public void requireSameLoginPw(PasswordEncoder passwordEncoder, String loginPw) {
        if (isNotSameLoginPw(passwordEncoder, loginPw)) {
            throw new KoinIllegalArgumentException("비밀번호가 일치하지 않습니다.");
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
            throw AuthenticationException.withDetail("탈퇴한 계정입니다. userId: " + id);
        }
    }

    private void ensurePermitted(UserType[] permittedUserTypes) {
        List<UserType> permittedUserTypesList = Arrays.asList(permittedUserTypes);
        if (permittedUserTypesList.contains(this.userType)) {
            return;
        }
        throw AuthorizationException.withDetail("인가되지 않은 유저 타입입니다. userId: " + id);
    }

    private void ensureAuthed() {
        if (isAuthed) {
            return;
        }
        switch (this.userType) {
            case OWNER -> throw AuthorizationException.withDetail("관리자 인증 대기중입니다. userId: " + id);
            case STUDENT -> throw AuthorizationException.withDetail("아우누리에서 인증메일을 확인해주세요. userId: " + id);
            case ADMIN -> throw AuthorizationException.withDetail("PL 인증 대기중입니다. userId: " + id);
            default -> throw AuthorizationException.withDetail("유효하지 않은 계정입니다. userId: " + id);
        }
    }

    public Integer authorizeAndGetId(UserType[] permittedUserTypes) {
        ensureNotDeleted();
        ensurePermitted(permittedUserTypes);
        ensureAuthed();
        return id;
    }
}
