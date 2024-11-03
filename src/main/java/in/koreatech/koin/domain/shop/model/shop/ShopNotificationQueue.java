package in.koreatech.koin.domain.shop.model.shop;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop_notification_queue")
public class ShopNotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @PositiveOrZero
    @Column(name = "shop_id", nullable = false)
    private Integer shopId;

    @NotNull
    @PositiveOrZero
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "notification_time", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime notificationTime;

    @Builder
    private ShopNotificationQueue(Integer shopId, Integer userId, LocalDateTime notificationTime) {
        this.shopId = shopId;
        this.userId = userId;
        this.notificationTime = notificationTime;
    }
}
