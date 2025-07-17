package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.util.ChatMessageIdGenerator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRedisRepository chatMessageRedisRepository;
    private final ChatMessageIdGenerator tsidGenerator;

    public void saveMessage(Integer articleId, Integer chatRoomId, Integer userId, Integer subscriptionCount,
        ChatMessage message) {
        boolean isRead = determineIsRead(subscriptionCount);

        ChatMessageEntity newMessage = ChatMessageEntity.create(articleId, chatRoomId, userId, isRead,
            message, tsidGenerator.generate());

        // 메시지 저장
        chatMessageRedisRepository.save(newMessage);
    }

    private boolean determineIsRead(Integer subscriptionCount) {
        return subscriptionCount == 2;
    }
}
