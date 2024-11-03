package in.koreatech.koin.domain.shop.model.event.listener;

import static in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType.REVIEW_PROMPT;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import in.koreatech.koin.domain.shop.model.event.dto.NotificationCreateEvent;
import in.koreatech.koin.domain.shop.model.shop.ShopNotificationQueue;
import in.koreatech.koin.domain.shop.repository.shop.ShopNotificationQueueRepository;
import in.koreatech.koin.global.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class NotificationEventListener {

    private final NotificationSubscribeRepository notificationSubscribeRepository;
    private final ShopNotificationQueueRepository shopNotificationQueueRepository;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onNotificationEventCreate(NotificationCreateEvent event) {
        if (isSubscribeReviewNotification(event)) {
            ShopNotificationQueue shopNotificationQueue = ShopNotificationQueue.builder()
                .shopId(event.shopId())
                .userId(event.studentId())
                .notificationTime(LocalDateTime.now().plusHours(1))
                .build();

            shopNotificationQueueRepository.save(shopNotificationQueue);
        }
    }

    private boolean isSubscribeReviewNotification(NotificationCreateEvent event) {
        return notificationSubscribeRepository
            .existsByUserIdAndSubscribeType(event.studentId(), REVIEW_PROMPT);
    }
}
