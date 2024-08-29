package in.koreatech.koin.global.domain.notification.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import in.koreatech.koin.domain.user.model.Device;
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
    @Column(name = "app_path")
    private MobileAppPath mobileAppPath;

    @Column(name = "scheme_uri")
    private String schemeUri;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(STRING)
    @Column(name = "type")
    private NotificationType type;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Notification(
        MobileAppPath appPath,
        String schemeUri,
        String title,
        String message,
        String imageUrl,
        NotificationType type,
        Device device
    ) {
        this.mobileAppPath = appPath;
        this.schemeUri = schemeUri;
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.type = type;
        this.device = device;
    }

    public void read() {
        this.isRead = true;
    }

    public String getType() {
        return type.name().toLowerCase();
    }
}
