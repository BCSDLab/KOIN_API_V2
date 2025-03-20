package in.koreatech.koin.socket.domain.message.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import in.koreatech.koin.socket.domain.message.model.ChatMessageCommand;
import in.koreatech.koin.socket.domain.notification.model.MessageReceivedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MessageEventHandler {

    private final ApplicationEventPublisher eventPublisher;

    public void onMessageReceived(Integer articleId, Integer chatRoomId, Integer userId, ChatMessageCommand messageCommand) {
        MessageReceivedEvent event = new MessageReceivedEvent(articleId, chatRoomId, userId, messageCommand);
        eventPublisher.publishEvent(event);
    }
}
