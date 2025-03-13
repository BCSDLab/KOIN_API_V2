package in.koreatech.koin.socket.domain.message.service.implement;

import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.domain.message.repository.ChatMessageRedisRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageUpdater {

    private final ChatMessageRedisRepository chatMessageRedisRepository;

    // 모든 메시지를 읽음 처리
    public void markMessageAsRead(Integer articleId, Integer chatRoomId, Integer userId) {
        chatMessageRedisRepository.markAllMessagesAsRead(articleId, chatRoomId, userId);
    }
}
