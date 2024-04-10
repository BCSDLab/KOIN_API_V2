package in.koreatech.koin.global.domain.notification.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.domain.BaseEntity;
import in.koreatech.koin.global.fcm.MobileAppPath;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notification")
@NoArgsConstructor(access = PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(name = "app_path", nullable = false)
    private MobileAppPath mobileAppPath;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Enumerated(STRING)
    @Column(nullable = false)
    private NotificationType type;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Notification(
        MobileAppPath appPath,
        String title,
        String message,
        String imageUrl,
        NotificationType type,
        User user
    ) {
        this.mobileAppPath = appPath;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.type = type;
        this.user = user;
    }

    public void read() {
        this.isRead = true;
    }

    public String getType() {
        return type.name().toLowerCase();
    }
}
