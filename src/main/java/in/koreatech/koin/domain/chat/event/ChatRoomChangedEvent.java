package in.koreatech.koin.domain.chat.event;

import in.koreatech.koin.domain.chat.model.ChatRoomKey;

public record ChatRoomChangedEvent(ChatRoomKey room) {
}
