package in.koreatech.koin.global.socket.domain.message.service.implement;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.global.socket.domain.message.model.ChatMessageCommand;
import in.koreatech.koin.global.socket.domain.message.model.ChatMessageEntity;
import in.koreatech.koin.global.socket.domain.message.repository.ChatMessageRedisRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageReader {

    private final ChatMessageRedisRepository chatMessageRepository;

    public List<ChatMessageCommand> allMessages(Integer articleId, Integer chatRoomId) {
        return chatMessageRepository.findByArticleIdAndChatRoomId(articleId, chatRoomId).stream()
            .map(ChatMessageCommand::toCommand)
            .toList();
    }

    public MessageSummary getMessageSummary(Integer articleId, Integer chatRoomId, Integer userId) {
        List<ChatMessageEntity> messages = chatMessageRepository.findByArticleIdAndChatRoomId(articleId, chatRoomId);

        if (messages.isEmpty()) {
            return null;
        }

        // 읽지 않은 메시지 수 계산
        int unreadCount = (int) messages.stream()
            .filter(message -> !message.getUserId().equals(userId)) // 상대방이 보낸 메시지
            .filter(message -> !message.getIsRead()) // 읽지 않은 메시지
            .count();

        // 최근 메시지 내용 조회
        String lastMessageContent = messages.stream()
            .max(Comparator.comparing(ChatMessageEntity::getId))
            .map(message -> message.getIsImage() ? "사진" : message.getContents())
            .orElse("");

        //최근 메시지 시간 조회
        LocalDateTime lastMessageTime = messages.stream()
            .max(Comparator.comparing(ChatMessageEntity::getId))
            .map(ChatMessageEntity::getCreatedAt)
            .orElse(null);

        return MessageSummary.builder()
            .unreadCount(unreadCount)
            .lastMessageContent(lastMessageContent)
            .lastMessageTime(lastMessageTime)
            .build();
    }

    @Getter
    @Builder
    public static class MessageSummary {
        private Integer unreadCount;
        private String lastMessageContent;
        private LocalDateTime lastMessageTime;
    }
}
