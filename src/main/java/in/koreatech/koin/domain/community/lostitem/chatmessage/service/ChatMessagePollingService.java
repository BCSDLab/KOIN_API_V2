package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatPresenceService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.PollingMessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessagePollingService {

    private final ChatMessageRedisRepository chatMessageRedisRepository;
    private final ChatRoomPollingSessionRedisRepository chatRoomPollingSessionRedisRepository;
    private final LostItemChatAccessService access;
    private final ChatPresenceService presence;
    private final UserRepository users;

    private final ApplicationEventPublisher eventPublisher;

    public List<ChatMessageResponse> pollAllMessages(Integer articleId, Integer chatRoomId, Integer userId) {
        access.validateParticipant(articleId, chatRoomId, userId);
        // 폴링할 때마다 사용자의 접속 상태를 레디스에 기록. 접속 상태인 사용자는 FCM 푸시 발송 X
        chatRoomPollingSessionRedisRepository.markUserOnline(articleId, chatRoomId, userId);

        // 상대방이 보낸 메시지를 읽음 처리
        advanceAllRead(articleId, chatRoomId, userId);

        return chatMessageRedisRepository.findByArticleIdAndChatRoomId(articleId, chatRoomId).stream()
            .map(ChatMessageResponse::toResponse)
            .toList();
    }

    public void markAllMessagesAsRead(Integer articleId, Integer chatRoomId, Integer userId) {
        access.validateParticipant(articleId, chatRoomId, userId);
        advanceAllRead(articleId, chatRoomId, userId);
    }

    public void markRead(Integer articleId, Integer chatRoomId, Integer userId, Long messageId) {
        access.validateAccess(articleId, chatRoomId, userId);
        if (chatMessageRedisRepository.markRead(articleId, chatRoomId, userId, messageId)) {
            publishReadChange(articleId, chatRoomId);
        }
    }

    private void advanceAllRead(Integer articleId, Integer chatRoomId, Integer userId) {
        if (chatMessageRedisRepository.markAllMessagesAsRead(articleId, chatRoomId, userId)) {
            publishReadChange(articleId, chatRoomId);
        }
    }

    private void publishReadChange(Integer articleId, Integer chatRoomId) {
        eventPublisher.publishEvent(new ChatRoomChangedEvent(
            new ChatRoomKey(ChatDomainType.LOST_ITEM, articleId, chatRoomId)));
    }

    public void leaveRoom(Integer articleId, Integer chatRoomId, Integer userId) {
        access.validateParticipant(articleId, chatRoomId, userId);
        chatRoomPollingSessionRedisRepository.markUserOffline(articleId, chatRoomId, userId);
    }

    public ChatMessageResponse sendMessage(Integer articleId, Integer chatRoomId, Integer userId, ChatMessage message) {
        return ChatMessageResponse.toResponse(sendLegacyMessageEntity(articleId, chatRoomId, userId, message));
    }

    public ChatMessageEntity sendMessageEntity(Integer articleId, Integer chatRoomId, Integer userId, ChatMessage message) {
        return saveMessage(articleId, chatRoomId, userId, message, false);
    }

    public ChatMessageEntity sendLegacyMessageEntity(Integer articleId, Integer chatRoomId, Integer userId,
        ChatMessage message) {
        return saveMessage(articleId, chatRoomId, userId, message, true);
    }

    private ChatMessageEntity saveMessage(Integer articleId, Integer chatRoomId, Integer userId,
        ChatMessage message, boolean legacy) {
        if (!legacy && (message == null || message.content() == null || message.content().isBlank()
            || message.isImage() == null)) {
            throw CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY);
        }
        var room = legacy ? access.validateParticipant(articleId, chatRoomId, userId)
            : access.validateAccess(articleId, chatRoomId, userId);
        Integer receiverId = userId.equals(room.getAuthorId()) ? room.getOwnerId() : room.getAuthorId();
        var roomKey = new ChatRoomKey(
            ChatDomainType.LOST_ITEM, articleId, chatRoomId);
        boolean isReceiverOnline = receiverId != null && (legacy
            ? chatRoomPollingSessionRedisRepository.isUserOnline(articleId, chatRoomId, receiverId)
            : presence.isOnline(roomKey, receiverId));
        boolean isRead = legacy && isReceiverOnline;
        ChatMessage canonical = legacy ? message : ChatMessage.builder()
            .userId(userId).userNickname(users.getById(userId).getDisplayNickname())
            .content(message.content()).isImage(message.isImage()).timestamp(LocalDateTime.now()).build();
        ChatMessageEntity saved = chatMessageRedisRepository.save(ChatMessageEntity.create(
            articleId, chatRoomId, userId, isRead, canonical, null));

        eventPublisher.publishEvent(new ChatMessageStoredEvent(roomKey,
            in.koreatech.koin.domain.chat.dto.ChatMessageResponse.builder()
                .messageId(saved.getId().toString()).userId(saved.getUserId()).userNickname(saved.getNickName())
                .content(saved.getContents()).timestamp(saved.getCreatedAt()).isImage(saved.getIsImage())
                .unreadCount(Boolean.TRUE.equals(saved.getIsRead()) ? 0 : 1).isLeftUser(false).build(), !legacy));
        if (!isRead && !isReceiverOnline && receiverId != null) {
            eventPublisher.publishEvent(new PollingMessageSendEvent(articleId, chatRoomId, userId, receiverId,
                new ChatMessage(saved.getUserId(), saved.getNickName(), saved.getContents(), saved.getCreatedAt(),
                    saved.getIsImage())));
        }
        return saved;
    }

}
