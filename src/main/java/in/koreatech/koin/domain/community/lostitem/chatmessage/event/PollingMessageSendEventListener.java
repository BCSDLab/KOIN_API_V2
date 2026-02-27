package in.koreatech.koin.domain.community.lostitem.chatmessage.event;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingNotificationSendService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class PollingMessageSendEventListener {

    private final ChatMessagePollingNotificationSendService notificationSendService;

    @Async
    @EventListener
    public void lostItemChatMessageReceived(PollingMessageSendEvent event) {
        notificationSendService.handle(event);
    }
}
