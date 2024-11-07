package in.koreatech.koin.domain.shop.model.shop;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import in.koreatech.koin.domain.user.model.User;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop_notification_buffer")
public class ShopNotificationBuffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", referencedColumnName = "id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "notification_time", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime notificationTime;

    @Builder
    private ShopNotificationBuffer(Shop shop, User user, LocalDateTime notificationTime) {
        this.shop = shop;
        this.user = user;
        this.notificationTime = notificationTime;
    }
}
