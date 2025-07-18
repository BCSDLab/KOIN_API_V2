package in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;


import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemChatUserBlockUseCase {

    private final LostItemChatUserBlockRepository userBlockRepository;
    private final LostItemChatRoomInfoRepository chatRoomInfoRepository;

    public void blockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        Integer otherUserId = findOtherUserId(articleId, chatRoomId, userId);

        if(alreadyBlockedByMe(userId, otherUserId)) {
            return;
        }

        updateOrCreateBlockInfo(userId, otherUserId);
    }

    public void unBlockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        Integer otherUserId = findOtherUserId(articleId, chatRoomId, userId);
        userBlockRepository.updateIsActiveByBlockerAndBlockedUser(userId, otherUserId, false);
    }

    public void validateBlockState(Integer articleId, Integer chatRoomId, Integer messageSendUserId) {
        Integer messageReceiveUserId = findOtherUserId(articleId, chatRoomId, messageSendUserId);

        boolean blockedByMessageSendUser = readByBlockerUserIdAndBlockedUserIdAndIsActive(
            messageSendUserId, messageReceiveUserId, true
        ).isPresent();

        boolean blockedByMessageReceiveUser = readByBlockerUserIdAndBlockedUserIdAndIsActive(
            messageReceiveUserId, messageSendUserId, true
        ).isPresent();

        if (blockedByMessageSendUser || blockedByMessageReceiveUser) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_BLOCKED_USER, "차단된 사용자입니다.");
        }
    }

    /* ======= 헬퍼 메소드 ======= */

    private Integer findOtherUserId(Integer articleId, Integer chatRoomId, Integer userId) {
        LostItemChatRoomInfoEntity chatRoomInfo = chatRoomInfoRepository.getByArticleIdAndChatRoomId(
            articleId, chatRoomId
        );

        Integer articleAuthorId = chatRoomInfo.getAuthorId();
        Integer ownerId = chatRoomInfo.getOwnerId();

        return userId.equals(articleAuthorId) ? ownerId : articleAuthorId;
    }

    private Boolean alreadyBlockedByMe(Integer userId, Integer otherUserId) {
        return userBlockRepository.findByBlockerUserAndBlockedUserAndIsActive(userId, otherUserId, true).isPresent();
    }

    private void updateOrCreateBlockInfo(Integer userId, Integer otherUserId) {
        var existingBlockInfo = readByBlockerUserIdAndBlockedUserIdAndIsActive(userId, otherUserId, false);
        if(existingBlockInfo.isPresent()) {
            userBlockRepository.updateIsActiveByBlockerAndBlockedUser(userId, otherUserId, true);
        } else {
            LostItemChatUserBlockEntity entity = LostItemChatUserBlockEntity.builder()
                .blockerUserId(userId)
                .blockedUserId(otherUserId)
                .isActive(true)
                .build();

            userBlockRepository.save(entity);
        }
    }

    private Optional<LostItemChatUserBlockEntity> readByBlockerUserIdAndBlockedUserIdAndIsActive(
        Integer blockerUserId, Integer blockedUserId, Boolean isActive
    ) {
        return userBlockRepository.findByBlockerUserAndBlockedUserAndIsActive(blockerUserId, blockedUserId, isActive);
    }

}
