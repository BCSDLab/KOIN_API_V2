package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin._common.model.MobileAppPath;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.MessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase.LostItemChatRoomInfoUseCase;
import in.koreatech.koin.domain.notification.model.Notification;
import in.koreatech.koin.domain.notification.model.NotificationFactory;
import in.koreatech.koin.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.domain.notification.repository.NotificationSubscribeRepository;
import in.koreatech.koin.domain.notification.service.NotificationService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.socket.session.model.WebSocketUserSession;
import in.koreatech.koin.socket.session.model.WebSocketUserSessionStatus;
import in.koreatech.koin.socket.session.service.WebSocketUserSessionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageSendService {

    private final NotificationService notificationService;
    private final NotificationFactory notificationFactory;
    private final NotificationSubscribeRepository notificationSubscribeRepository;

    private final LostItemChatRoomInfoRepository chatRoomInfoRepository;
    private final UserRepository userRepository;

    private final WebSocketUserSessionService webSocketUserSessionService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    private final LostItemChatRoomInfoUseCase chatRoomInfoUseCase;

    @Transactional(readOnly = true)
    public void handle(MessageSendEvent event) {
        Integer partnerId = getPartnerId(
            event.articleId(), event.chatRoomId(), event.userId()
        );

        // 메시지를 받는 사용자의 채팅방 목록 정보를 업데이트
        String destination = "/topic/chatroom/list/" + partnerId;
        var chatRoomInfo = chatRoomInfoUseCase.getAllChatRoomInfo(partnerId);
        simpMessageSendingOperations.convertAndSend(destination, chatRoomInfo);

        // 메시지를 받는 사용자에게 FCM 알림 전송
        if (!isNotificationEligible(partnerId)) {
            return;
        }

        User sender = userRepository.getById(event.userId());
        User partner = userRepository.getById(partnerId);

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

    /**
     * 알림 전송 가능 여부를 확인하는 메서드 <br>
     * 현재 웹소켓 연결이 끊겨 있고, 디바이스 토큰이 존재 하며, 채팅 알림 받기를 설정한 사용자에게만 푸시 전송
     */
    private boolean isNotificationEligible(Integer partnerId) {
        return !isActiveSession(partnerId) &&
            hasDeviceToken(partnerId) &&
            isSubscribedToChatNotification(partnerId);
    }

    /**
     * 웹소켓 세션이 활성 상태인지 확인
     */
    private boolean isActiveSession(Integer partnerId) {
        return webSocketUserSessionService.read(partnerId)
            .map(WebSocketUserSession::getStatus)
            .filter(session ->
                WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM.equals(session) ||
                    WebSocketUserSessionStatus.ACTIVE_CHAT_ROOM_LIST.equals(session))
            .isPresent();
    }

    /**
     * 사용자가 디바이스 토큰을 가지고 있는지 확인
     */
    private boolean hasDeviceToken(Integer partnerId) {
        User partner = userRepository.getById(partnerId);
        return partner.getDeviceToken() != null;
    }

    /**
     * 사용자가 채팅 알림을 설정했는지 확인
     */
    private boolean isSubscribedToChatNotification(Integer partnerId) {
        return notificationSubscribeRepository.existsByUserIdAndSubscribeTypeAndDetailTypeIsNull(partnerId, NotificationSubscribeType.LOST_ITEM_CHAT);
    }

    /**
     * 상대방 userId 얻기
     */
    private Integer getPartnerId(Integer articleId, Integer chatRoomId, Integer userId) {
        var chatRoomInfo = chatRoomInfoRepository.getByArticleIdAndChatRoomId(articleId, chatRoomId);

        Map<Integer, Integer> partnerMap = new HashMap<>();
        partnerMap.put(chatRoomInfo.getAuthorId(), chatRoomInfo.getOwnerId());
        partnerMap.put(chatRoomInfo.getOwnerId(), chatRoomInfo.getAuthorId());
        return partnerMap.get(userId);
    }
}
