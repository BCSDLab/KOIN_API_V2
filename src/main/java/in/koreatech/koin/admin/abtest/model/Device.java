package in.koreatech.koin.admin.abtest.model;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.time.LocalDateTime;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "device", schema = "koin")
public class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 100)
    @Column(name = "model", length = 100)
    private String model;

    @Size(max = 100)
    @Column(name = "os", length = 100)
    private String os;

    @Size(max = 255)
    @Column(name = "fcm_token")
    private String fcmToken;

    @NotNull
    @Column(name = "last_accessed_at", nullable = false)
    private LocalDateTime lastAccessedAt;

    @Builder
    private Device(
        Integer id,
        User user,
        String model,
        String os,
        String fcmToken,
        LocalDateTime lastAccessedAt
    ) {
        this.id = id;
        this.user = user;
        this.model = model;
        this.os = os;
        this.fcmToken = fcmToken;
        this.lastAccessedAt = lastAccessedAt;
    }
}
