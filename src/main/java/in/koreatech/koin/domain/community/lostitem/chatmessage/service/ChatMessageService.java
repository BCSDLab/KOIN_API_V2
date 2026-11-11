package in.koreatech.koin.domain.community.lostitem.chatmessage.service;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRedisRepository messages;
    private final LostItemChatAccessService access;

    public ChatMessageEntity saveMessage(Integer articleId, Integer chatRoomId, Integer userId,
        Integer subscriptionCount, ChatMessage message) {
        access.validateParticipant(articleId, chatRoomId, userId);
        return messages.save(ChatMessageEntity.create(articleId, chatRoomId, userId,
            subscriptionCount == 2, message, null));
    }
}
