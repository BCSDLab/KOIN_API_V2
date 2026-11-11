package in.koreatech.koin.domain.community.lostitem.chatroom.service;

import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemChatMessageService {

    private final ChatMessageRedisRepository chatMessageRedisRepository;

    private final ChatMessagePollingService polling;

    public List<ChatMessageResponse> getAllMessages(Integer articleId, Integer chatRoomId, Integer userId) {
        // 상대방이 보낸 메시지를 읽음 처리
        polling.markAllMessagesAsRead(articleId, chatRoomId, userId);

        return chatMessageRedisRepository.findByArticleIdAndChatRoomId(articleId, chatRoomId).stream()
            .map(ChatMessageResponse::toResponse)
            .toList();
    }
}
