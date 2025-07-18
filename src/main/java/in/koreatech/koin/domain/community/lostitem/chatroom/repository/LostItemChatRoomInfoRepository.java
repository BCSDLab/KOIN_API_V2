package in.koreatech.koin.domain.community.lostitem.chatroom.repository;

import java.util.List;
import java.util.Optional;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;

public interface LostItemChatRoomInfoRepository {

    LostItemChatRoomInfoEntity save(LostItemChatRoomInfoEntity chatRoomInfo);
    List<LostItemChatRoomInfoEntity> findByArticleId(Integer articleId);
    Optional<LostItemChatRoomInfoEntity> findByArticleIdAndMessageSenderId(Integer articleId, Integer messageSenderId);
    LostItemChatRoomInfoEntity getByArticleIdAndChatRoomId(Integer articleId, Integer chatRoomId);
    List<LostItemChatRoomInfoEntity> findByUserId(Integer userId);
}
