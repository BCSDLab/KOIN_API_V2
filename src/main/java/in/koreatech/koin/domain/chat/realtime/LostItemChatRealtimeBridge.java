package in.koreatech.koin.domain.chat.realtime;

import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.chat.dto.ChatRealtimeEvent;
import in.koreatech.koin.domain.chat.event.ChatRealtimeReceivedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.service.ChatService;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LostItemChatRealtimeBridge {
    private final SimpMessageSendingOperations broker;
    private final LostItemChatRoomInfoRepository rooms;
    private final LostItemChatRoomInfoService roomService;
    private final ChatService chatService;

    @EventListener
    public void onReceived(ChatRealtimeReceivedEvent received) {
        if (!received.relayLegacy()) {
            return;
        }
        var event = received.event();
        ChatRoomKey key = ChatRoomKey.parse(event.roomId());
        if (key.domainType() != ChatDomainType.LOST_ITEM) {
            return;
        }
        if (event.type() == ChatRealtimeEvent.Type.MESSAGE_CREATED) {
            var message = event.message();
            try {
                broker.convertAndSend("/topic/chat/" + key.contextId() + "/" + key.chatRoomId(),
                    new ChatMessageResponse(message.userId(), message.userNickname(), message.content(),
                        message.timestamp(), message.isImage()));
            } catch (RuntimeException exception) {
                log.warn("Legacy chat relay failed: room={}, error={}", key.encoded(),
                    exception.getClass().getSimpleName());
            }
        }
        try {
            var room = rooms.getByArticleIdAndChatRoomId(key.contextId(), key.chatRoomId());
            Stream.of(room.getAuthorId(), room.getOwnerId()).filter(Objects::nonNull).distinct()
                .forEach(memberId -> refreshList(key, memberId));
        } catch (RuntimeException exception) {
            log.warn("Legacy chat member lookup failed: room={}, error={}", key.encoded(),
                exception.getClass().getSimpleName());
        }
    }

    private void refreshList(ChatRoomKey key, Integer memberId) {
        try {
            try {
                chatService.validateReadAccess(memberId, key.encoded());
            } catch (CustomException exception) {
                int status = exception.getErrorCode().getHttpStatus().value();
                if (status == 401 || status == 403 || status == 404) {
                    log.debug("Legacy chat list skipped: room={}, member={}, error={}", key.encoded(), memberId,
                        exception.getClass().getSimpleName());
                    return;
                }
                throw exception;
            }
            broker.convertAndSend("/topic/chatroom/list/" + memberId, roomService.getAllChatRoomInfo(memberId));
        } catch (RuntimeException exception) {
            StackTraceElement[] trace = exception.getStackTrace();
            log.warn("Legacy chat list refresh failed: room={}, member={}, error={}, source={}",
                key.encoded(), memberId, exception.getClass().getSimpleName(),
                trace.length == 0 ? "unknown" : trace[0].toString());
        }
    }
}
