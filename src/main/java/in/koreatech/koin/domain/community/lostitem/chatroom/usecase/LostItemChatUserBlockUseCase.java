package in.koreatech.koin.domain.community.lostitem.chatroom.usecase;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemChatUserBlockUseCase {

    private final LostItemChatUserBlockService lostItemChatUserBlockService;

    public void blockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        lostItemChatUserBlockService.blockUser(articleId, chatRoomId, userId);
    }

    public void unblockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        lostItemChatUserBlockService.unBlockUser(articleId, chatRoomId, userId);
    }
}
