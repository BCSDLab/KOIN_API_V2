package in.koreatech.koin.domain.user;

import in.koreatech.koin.domain.BaseEntity;
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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Size(max = 20)
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
    private Instant lastLoggedAt;

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
    private User(String password, String nickname, String name, String phoneNumber, UserType userType, String email,
                 UserGender gender, Boolean isAuthed, Instant lastLoggedAt, String profileImageUrl, Boolean isDeleted,
                 String authToken, String authExpiredAt, String resetToken, String resetExpiredAt) {
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
}
