package in.koreatech.koin.domain.chat.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record ChatRealtimeEvent(Type type, String roomId, ChatMessageResponse message) {

    public enum Type {
        MESSAGE_CREATED,
        ROOM_CHANGED
    }
}
