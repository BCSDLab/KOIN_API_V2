package in.koreatech.koin.socket.domain.notification.service;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationSubscribeReader {

    private final NotificationSubscribeRepository notificationSubscribeRepository;

    public boolean isSubscribeChatNotification(Integer userId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(userId, NotificationSubscribeType.LOST_ITEM_CHAT);
    }
}
