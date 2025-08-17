package in.koreatech.koin.domain.community.lostitem.chatmessage.dto;

import java.time.LocalDateTime;

public record ChatMessageSummary(
    Integer unreadCount,
    String lastMessageContent,
    LocalDateTime lastMessageTime
) {

    public static ChatMessageSummary from(Integer unreadCount, String lastMessageContent, LocalDateTime lastMessageTime) {
        return new ChatMessageSummary(unreadCount, lastMessageContent, lastMessageTime);
    }
}
