package in.koreatech.koin.socket.domain.notification.service;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin._common.model.MobileAppPath;
import in.koreatech.koin.socket.domain.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.socket.domain.chatroom.service.implement.ChatRoomInfoReader;
import in.koreatech.koin.socket.domain.notification.model.MessageReceivedEvent;
import in.koreatech.koin.socket.domain.session.model.UserSession;
import in.koreatech.koin.socket.domain.session.model.UserSessionStatus;
import in.koreatech.koin.socket.domain.session.service.UserSessionService;
import in.koreatech.koin.socket.domain.session.service.implement.UserReader;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class MessageReceivedEventListener {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeReader subscribeReader;
    private final ChatRoomInfoReader chatRoomInfoReader;
    private final UserReader userReader;
    private final UserSessionService userSessionService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final LostItemChatRoomInfoService chatRoomInfoService;

    @EventListener
    public void lostItemChatMessageReceived(MessageReceivedEvent event) {
        Integer partnerId = chatRoomInfoReader.getPartnerId(
            event.articleId(), event.chatRoomId(), event.userId()
        );

        String destination = "/topic/chatroom/list/" + partnerId;
        var chatRoomInfo = chatRoomInfoService.getAllChatRoomInfo(partnerId);
        simpMessageSendingOperations.convertAndSend(destination, chatRoomInfo);

        if (!isNotificationEligible(partnerId)) {
            return;
        }

        User sender = userReader.readUser(event.userId());
        User partner = userReader.readUser(partnerId);

        Notification notification = notificationFactory.generateChatMessageNotification(
            MobileAppPath.CHAT,
            event.articleId(),
            event.chatRoomId(),
            sender.getNickname(),
            event.messageCommand().content(),
            partner
        );

        notificationService.pushNotification(notification);
    }

    /**
     * 알림 전송 가능 여부를 확인하는 메서드
     */
    private boolean isNotificationEligible(Integer partnerId) {
        return !isActiveSession(partnerId) &&
            hasDeviceToken(partnerId) &&
            isSubscribedToChatNotification(partnerId);
    }

    /**
     * 세션이 활성 상태인지 확인
     */
    private boolean isActiveSession(Integer partnerId) {
        return userSessionService.read(partnerId)
            .map(UserSession::getStatus)
            .filter(session ->
                UserSessionStatus.ACTIVE_CHAT_ROOM.equals(session) ||
                UserSessionStatus.ACTIVE_CHAT_ROOM_LIST.equals(session))
            .isPresent();
    }

    /**
     * 사용자가 디바이스 토큰을 가지고 있는지 확인
     */
    private boolean hasDeviceToken(Integer partnerId) {
        User partner = userReader.readUser(partnerId);
        return partner.getDeviceToken() != null;
    }

    /**
     * 사용자가 채팅 알림을 설정했는지 확인
     */
    private boolean isSubscribedToChatNotification(Integer partnerId) {
        return subscribeReader.isSubscribeChatNotification(partnerId);
    }
}
