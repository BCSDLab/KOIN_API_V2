package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.PollingMessageSendEvent;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessagePollingNotificationSendService {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;

    private final UserRepository userRepository;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    @Transactional
    public void handle(PollingMessageSendEvent event) {
        if (!isNotificationEligible(event.receiverId())) {
            return;
        }

        User sender = userRepository.getById(event.userId());
        User partner = userRepository.getById(event.receiverId());

        Notification notification = notificationFactory.generateChatMessageNotification(
            MobileAppPath.CHAT,
            event.articleId(),
            event.chatRoomId(),
            sender.getNickname(),
            event.chatMessage().content(),
            partner
        );

        notificationService.pushNotification(notification);
    }

    private boolean isNotificationEligible(Integer partnerId) {
        return hasDeviceToken(partnerId) && isSubscribedToChatNotification(partnerId);
    }

    private boolean hasDeviceToken(Integer partnerId) {
        User partner = userRepository.getById(partnerId);
        return partner.getDeviceToken() != null;
    }

    private boolean isSubscribedToChatNotification(Integer partnerId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(partnerId, NotificationSubscribeType.LOST_ITEM_CHAT);
    }
}
