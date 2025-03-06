package in.koreatech.koin.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Clock;
import java.time.LocalDateTime;

import org.hibernate.annotations.Where;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin._common.converter.LocalDateTimeAttributeConverter;
import in.koreatech.koin._common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "nickname", length = 50, unique = true)
    private String nickname;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Size(max = 100)
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gender", columnDefinition = "INT")
    @Enumerated(value = EnumType.ORDINAL)
    private UserGender gender;

    @NotNull
    @Column(name = "is_authed", nullable = false)
    private boolean isAuthed = false;

    @Column(name = "last_logged_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLoggedAt;

    @Size(max = 255)
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Size(max = 255)
    @Column(name = "reset_token")
    private String resetToken;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "reset_expired_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime resetExpiredAt;

    @Column(name = "device_token", nullable = true)
    private String deviceToken;

    @Builder
    private User(
        String password,
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
        String resetToken,
        LocalDateTime resetExpiredAt,
        String deviceToken
    ) {
        this.password = password;
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
        this.resetToken = resetToken;
        this.resetExpiredAt = resetExpiredAt;
        this.deviceToken = deviceToken;
    }

    private static final int ONE_HOUR = 1;

    public void generateResetTokenForFindPassword(Clock clock) {
        this.resetExpiredAt = LocalDateTime.now(clock).plusHours(ONE_HOUR);
        this.resetToken = this.email + this.resetExpiredAt;
    }

    public void update(String nickname, String name, String phoneNumber, UserGender gender) {
        this.nickname = nickname;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateLastLoggedTime(LocalDateTime lastLoggedTime) {
        lastLoggedAt = lastLoggedTime;
    }
    public void updatePassword(PasswordEncoder passwordEncoder, String password) {
        this.password = passwordEncoder.encode(password);
    }

    public void updateStudentPassword(PasswordEncoder passwordEncoder, String password) {
        if (password != null && !password.isEmpty())
            this.password = passwordEncoder.encode(password);
    }

    public boolean isSamePassword(PasswordEncoder passwordEncoder, String password) {
        return passwordEncoder.matches(password, this.password);
    }

    public void auth() {
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
}
