package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.common.model.MobileAppPath;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
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
        User partner = userRepository.getById(event.receiverId());
        if (partner.getDeviceToken() == null || !isSubscribedToChatNotification(event.receiverId())) {
            return;
        }
        User sender = userRepository.getById(event.userId());
        ChatRoomKey room = new ChatRoomKey(ChatDomainType.LOST_ITEM, event.articleId(), event.chatRoomId());

        Notification notification = notificationFactory.generateChatMessageNotification(
            MobileAppPath.CHAT,
            event.articleId(),
            event.chatRoomId(),
            sender.getDisplayNickname(),
            event.chatMessage().content(),
            partner
        );

        notificationService.pushChatNotification(notification, room.encoded());
    }

    private boolean isSubscribedToChatNotification(Integer partnerId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(partnerId, NotificationSubscribeType.LOST_ITEM_CHAT);
    }
}
