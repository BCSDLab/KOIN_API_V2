package in.koreatech.koin.domain.community.lostitem.chatroom.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomInfoResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemArticleInfoService;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatUserBlockService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemChatRoomUseCase {

    private final LostItemChatRoomInfoService chatRoomInfoService;
    private final LostItemChatUserBlockService chatUserBlockService;
    private final LostItemArticleInfoService articleInfoService;

    @Transactional
    public ChatRoomInfoResponse getOrCreateLostItemChatRoom(Integer lostItemArticleId, Integer messageSenderId) {
        Integer chatRoomId = chatRoomInfoService.getOrCreateChatRoomId(lostItemArticleId, messageSenderId);
        chatUserBlockService.validateBlockState(lostItemArticleId, chatRoomId, messageSenderId);

        String articleTitle = articleInfoService.getArticleTitle(lostItemArticleId);
        String chatPartnerProfileImage = articleInfoService.getChatPartnerProfileImage(lostItemArticleId);

        return ChatRoomInfoResponse.from(
            lostItemArticleId, chatRoomId, messageSenderId, articleTitle, chatPartnerProfileImage
        );
    }

    @Transactional(readOnly = true)
    public ChatRoomInfoResponse getLostItemChatRoom(Integer lostItemArticleId, Integer messageSenderId, Integer chatRoomId) {
        chatUserBlockService.validateBlockState(lostItemArticleId, chatRoomId, messageSenderId);

        String articleTitle = articleInfoService.getArticleTitle(lostItemArticleId);
        String chatPartnerProfileImage = articleInfoService.getChatPartnerProfileImage(lostItemArticleId);
        return ChatRoomInfoResponse.from(
            lostItemArticleId, chatRoomId, messageSenderId, articleTitle, chatPartnerProfileImage
        );
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getAllChatRoomInfoByUserId(Integer userId) {
        return chatRoomInfoService.getAllChatRoomInfo(userId);
    }
}
