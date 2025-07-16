package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageCommand;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.util.ChatMessageIdGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRedisRepository chatMessageRedisRepository;
    private final ChatMessageIdGenerator tsidGenerator;

    public void saveMessage(Integer articleId, Integer chatRoomId, Integer userId, Integer subscriptionCount, ChatMessageCommand message) {
        boolean isRead = determineIsRead(subscriptionCount);

        ChatMessageEntity newMessage = ChatMessageEntity.create(articleId, chatRoomId, userId, isRead, message);

        // 메시지 TSID 생성
        generateMessageId(newMessage);

        // 메시지 저장
        chatMessageRedisRepository.save(newMessage);
    }

    private boolean determineIsRead(Integer subscriptionCount) {
        return subscriptionCount == 2;
    }

    private void generateMessageId(ChatMessageEntity message) {
        message.addMessageId(tsidGenerator.generate());
    }
}
