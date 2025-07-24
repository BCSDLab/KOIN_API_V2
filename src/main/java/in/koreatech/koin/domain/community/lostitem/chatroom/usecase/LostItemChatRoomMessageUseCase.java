package in.koreatech.koin.domain.community.lostitem.chatroom.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatMessageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemChatRoomMessageUseCase {

    private final LostItemChatMessageService chatMessageService;

    public List<ChatMessageResponse> getAllChatRoomMessages(Integer articleId, Integer chatRoomId, Integer userId) {
        return chatMessageService.getAllMessages(articleId, chatRoomId, userId);
    }
}
