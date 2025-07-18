package in.koreatech.koin.domain.community.lostitem.chatroom.repository;

import java.util.Optional;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatUserBlockEntity;

public interface LostItemChatUserBlockRepository {

    LostItemChatUserBlockEntity save(LostItemChatUserBlockEntity entity);

    Optional<LostItemChatUserBlockEntity> findByBlockerUserAndBlockedUserAndIsActive(
        Integer blockerUserId,
        Integer blockedUserId,
        Boolean isActive
    );

    void updateIsActiveByBlockerAndBlockedUser(
        Integer blockerUserId,
        Integer blockedUserId,
        Boolean isActive
    );
}
