package in.koreatech.koin.domain.community.lostitem.chatroom.service;

import java.util.Optional;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemChatUserBlockService {

    private final LostItemChatUserBlockRepository userBlockRepository;
    private final LostItemChatAccessService access;

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
        access.validateAccess(articleId, chatRoomId, messageSendUserId);
    }

    /* ======= 헬퍼 메소드 ======= */

    private Integer findOtherUserId(Integer articleId, Integer chatRoomId, Integer userId) {
        LostItemChatRoomInfoEntity chatRoomInfo = access.validateParticipant(articleId, chatRoomId, userId);

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
