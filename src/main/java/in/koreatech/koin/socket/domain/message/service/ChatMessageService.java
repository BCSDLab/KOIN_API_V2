package in.koreatech.koin.socket.domain.message.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.socket.domain.message.model.ChatMessageCommand;
import in.koreatech.koin.socket.domain.message.model.ChatMessageEntity;
import in.koreatech.koin.socket.domain.message.service.implement.MessageAppender;
import in.koreatech.koin.socket.domain.message.service.implement.MessageHelper;
import in.koreatech.koin.socket.domain.message.service.implement.MessageReader;
import in.koreatech.koin.socket.domain.message.service.implement.MessageUpdater;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final MessageHelper messageHelper;
    private final MessageAppender messageAppender;
    private final MessageReader messageReader;
    private final MessageUpdater messageUpdater;

    public void saveMessage(Integer articleId, Integer chatRoomId, Integer userId, Integer subscriptionCount, ChatMessageCommand message) {
        boolean isRead = determineIsRead(subscriptionCount);

        ChatMessageEntity newMessage = ChatMessageEntity.create(articleId, chatRoomId, userId, isRead, message);

        // 메시지 TSID 생성
        messageHelper.generateMessageId(newMessage);

        // 메시지 저장
        messageAppender.save(newMessage);
    }

    public List<ChatMessageCommand> readMessages(Integer articleId, Integer chatRoomId, Integer userId) {
        // 상대방이 보낸 메시지를 읽음 처리
        messageUpdater.markMessageAsRead(articleId, chatRoomId, userId);

        // 메시지 조회
        return messageReader.allMessages(articleId, chatRoomId);
    }

    private boolean determineIsRead(Integer subscriptionCount) {
        return subscriptionCount == 2;
    }
}
