package in.koreatech.koin.domain.community.lostitem.chatroom.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase.LostItemArticleInfoUseCase;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase.LostItemChatRoomInfoUseCase;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase.LostItemChatMessageUseCase;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase.LostItemChatUserBlockUseCase;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomInfoResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemChatRoomService {

    private final LostItemChatRoomInfoUseCase chatRoomInfoUseCase;
    private final LostItemChatUserBlockUseCase chatRoomUserBlockUseCase;
    private final LostItemArticleInfoUseCase articleInfoUseCase;
    private final LostItemChatMessageUseCase chatMessageUseCase;

    public ChatRoomInfoResponse getOrCreateLostItemChatRoom(Integer lostItemArticleId, Integer messageSenderId) {
        Integer chatRoomId = chatRoomInfoUseCase.getOrCreateChatRoomId(lostItemArticleId, messageSenderId);
        chatRoomUserBlockUseCase.validateBlockState(lostItemArticleId, chatRoomId, messageSenderId);

        String articleTitle = articleInfoUseCase.getArticleTitle(lostItemArticleId);
        String chatPartnerProfileImage = articleInfoUseCase.getChatPartnerProfileImage(lostItemArticleId);

        return ChatRoomInfoResponse.from(
            lostItemArticleId, chatRoomId, messageSenderId, articleTitle, chatPartnerProfileImage
        );
    }

    @Transactional(readOnly = true)
    public ChatRoomInfoResponse getLostItemChatRoom(Integer lostItemArticleId, Integer messageSenderId, Integer chatRoomId) {
        chatRoomUserBlockUseCase.validateBlockState(lostItemArticleId, chatRoomId, messageSenderId);

        String articleTitle = articleInfoUseCase.getArticleTitle(lostItemArticleId);
        String chatPartnerProfileImage = articleInfoUseCase.getChatPartnerProfileImage(lostItemArticleId);
        return ChatRoomInfoResponse.from(
            lostItemArticleId, chatRoomId, messageSenderId, articleTitle, chatPartnerProfileImage
        );
    }

    public void blockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        chatRoomUserBlockUseCase.blockUser(articleId, chatRoomId, userId);
    }

    public void unblockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        chatRoomUserBlockUseCase.unBlockUser(articleId, chatRoomId, userId);
    }

    public List<ChatRoomListResponse> getAllChatRoomInfoByUserId(Integer userId) {
        return chatRoomInfoUseCase.getAllChatRoomInfo(userId);
    }

    public List<ChatMessageResponse> getAllMessages(Integer articleId, Integer chatRoomId, Integer userId) {
        return chatMessageUseCase.getAllMessages(articleId, chatRoomId, userId);
    }
}
