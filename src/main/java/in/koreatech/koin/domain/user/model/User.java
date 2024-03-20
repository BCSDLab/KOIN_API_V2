package in.koreatech.koin.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Lob
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "nickname", length = 50)
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
    @NotNull
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "gender")
    @Enumerated(value = EnumType.ORDINAL)
    private UserGender gender;

    @NotNull
    @Column(name = "is_authed", nullable = false)
    private Boolean isAuthed = false;

    @Column(name = "last_logged_at")
    private LocalDateTime lastLoggedAt;

    @Size(max = 255)
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Size(max = 255)
    @Column(name = "auth_token")
    private String authToken;

    @Size(max = 255)
    @Column(name = "auth_expired_at")
    private String authExpiredAt;

    @Size(max = 255)
    @Column(name = "reset_token")
    private String resetToken;

    @Size(max = 255)
    @Column(name = "reset_expired_at")
    private String resetExpiredAt;

    @Builder
    private User(String password, String nickname, String name, String phoneNumber, UserType userType,
        String email, UserGender gender, Boolean isAuthed, LocalDateTime lastLoggedAt, String profileImageUrl,
        Boolean isDeleted, String authToken, String authExpiredAt, String resetToken, String resetExpiredAt) {
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
        this.authToken = authToken;
        this.authExpiredAt = authExpiredAt;
        this.resetToken = resetToken;
        this.resetExpiredAt = resetExpiredAt;
    }

    public boolean isSamePassword(String password) {
        return this.password.equals(password);
    }

    public void updateLastLoggedTime(LocalDateTime lastLoggedTime) {
        lastLoggedAt = lastLoggedTime;
    }

    public void update(User user) {
        if (user.password != null) {
            this.password = user.password;
        }
        if (user.nickname != null) {
            this.nickname = user.nickname;
        }
        if (user.name != null) {
            this.name = user.name;
        }
        if (user.phoneNumber != null) {
            this.phoneNumber = user.phoneNumber;
        }
        if (user.email != null) {
            this.email = user.email;
        }
        if (user.gender != null) {
            this.gender = user.gender;
        }
        if (user.isAuthed != null) {
            this.isAuthed = user.isAuthed;
        }
        if (user.lastLoggedAt != null) {
            this.lastLoggedAt = user.lastLoggedAt;
        }
        if (user.profileImageUrl != null) {
            this.profileImageUrl = user.profileImageUrl;
        }
        if (user.authToken != null) {
            this.authToken = user.authToken;
        }
        if (user.authExpiredAt != null) {
            this.authExpiredAt = user.authExpiredAt;
        }
        if (user.resetToken != null) {
            this.resetToken = user.resetToken;
        }
        if (user.resetExpiredAt != null) {
            this.resetExpiredAt = user.resetExpiredAt;
        }
    }
}
