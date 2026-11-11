package in.koreatech.koin.domain.chat.event;

import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;

public record ChatMessageStoredEvent(ChatRoomKey room, ChatMessageResponse message, boolean relayLegacy) {
    public ChatMessageStoredEvent(ChatRoomKey room, ChatMessageResponse message) {
        this(room, message, false);
    }
}
