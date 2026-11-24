package in.koreatech.koin.domain.chat.event;

import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;

public record ChatRealtimeReceivedEvent(ChatRealtimeEvent event, boolean relayLegacy) {
    public ChatRealtimeReceivedEvent(ChatRealtimeEvent event) {
        this(event, false);
    }
}
