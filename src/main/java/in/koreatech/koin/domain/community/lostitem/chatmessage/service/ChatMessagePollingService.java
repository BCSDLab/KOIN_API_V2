package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.tsid.TsidCreator;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.event.PollingMessageSendEvent;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatRoomPollingSessionRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessagePollingService {

    private final ChatMessageRedisRepository chatMessageRedisRepository;
    private final ChatRoomPollingSessionRedisRepository chatRoomPollingSessionRedisRepository;
    private final LostItemChatRoomInfoRepository chatRoomInfoRepository;

    private final ApplicationEventPublisher eventPublisher;

    public List<ChatMessageResponse> pollAllMessages(Integer articleId, Integer chatRoomId, Integer userId) {
        // 폴링할 때마다 사용자의 접속 상태를 레디스에 기록. 접속 상태인 사용자는 FCM 푸시 발송 X
        chatRoomPollingSessionRedisRepository.markUserOnline(articleId, chatRoomId, userId);

        // 상대방이 보낸 메시지를 읽음 처리
        chatMessageRedisRepository.markAllMessagesAsRead(articleId, chatRoomId, userId);

        return chatMessageRedisRepository.findByArticleIdAndChatRoomId(articleId, chatRoomId).stream()
            .map(ChatMessageResponse::toResponse)
            .toList();
    }

    public void leaveRoom(Integer articleId, Integer chatRoomId, Integer userId) {
        chatRoomPollingSessionRedisRepository.markUserOffline(articleId, chatRoomId, userId);
    }

    public ChatMessageResponse sendMessage(Integer articleId, Integer chatRoomId, Integer userId, ChatMessage message) {
        // 채팅 상대방의 접속 상태를 확인
        Integer receiverId = getPartnerId(articleId, chatRoomId, userId);
        Boolean isReceiverOnline = chatRoomPollingSessionRedisRepository.isUserOnline(articleId, chatRoomId, receiverId);

        ChatMessageEntity newMessage = ChatMessageEntity.create(
            articleId,
            chatRoomId,
            userId,
            isReceiverOnline, // 채팅방에 접속중이면 메시지 바로 읽음 처리
            message,
            generateUniqueIdentifier()
        );

        chatMessageRedisRepository.save(newMessage);

        // 상대방이 접속 중이 아니면 FCM 알림 발송
        if (!isReceiverOnline && receiverId != null) {
            eventPublisher.publishEvent(
                new PollingMessageSendEvent(articleId, chatRoomId, userId, receiverId, message)
            );
        }

        return ChatMessageResponse.toResponse(newMessage);
    }

    private Integer getPartnerId(Integer articleId, Integer chatRoomId, Integer userId) {
        var chatRoomInfo = chatRoomInfoRepository.getByArticleIdAndChatRoomId(articleId, chatRoomId);

        Map<Integer, Integer> partnerMap = new HashMap<>();
        partnerMap.put(chatRoomInfo.getAuthorId(), chatRoomInfo.getOwnerId());
        partnerMap.put(chatRoomInfo.getOwnerId(), chatRoomInfo.getAuthorId());
        return partnerMap.get(userId);
    }

    private Long generateUniqueIdentifier() {
        return TsidCreator.getTsid256().toLong();
    }
}
