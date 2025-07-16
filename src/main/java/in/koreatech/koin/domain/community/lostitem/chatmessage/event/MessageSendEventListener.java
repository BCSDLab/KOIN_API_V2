package in.koreatech.koin.domain.community.lostitem.chatmessage.event;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessageSendService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class MessageSendEventListener {

    private final ChatMessageSendService chatMessageSendService;

    @EventListener
    public void lostItemChatMessageReceived(MessageSendEvent event) {
        chatMessageSendService.handle(event);
    }
}
