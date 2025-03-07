package in.koreatech.koin.socket.domain.notification.service;

import org.springframework.stereotype.Component;

import in.koreatech.koin.integration.fcm.notification.model.NotificationSubscribeType;
import in.koreatech.koin.integration.fcm.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationSubscribeReader {

    private final NotificationSubscribeRepository notificationSubscribeRepository;

    public boolean isSubscribeChatNotification(Integer userId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeType(userId, NotificationSubscribeType.LOST_ITEM_CHAT);
    }
}
