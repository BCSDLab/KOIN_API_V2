package in.koreatech.koin.domain.community.lostitem.chatroom.service;

import java.util.Objects;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemChatAccessService {
    private final LostItemChatRoomInfoRepository rooms;
    private final LostItemChatUserBlockRepository blocks;

    public LostItemChatRoomInfoEntity validateParticipant(Integer articleId, Integer roomId, Integer userId) {
        var room = rooms.getByArticleIdAndChatRoomId(articleId, roomId);
        if (!Objects.equals(articleId, room.getArticleId()) || !Objects.equals(roomId, room.getChatRoomId())) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_LOST_ITEM_CHATROOM);
        }
        if (userId == null || (!userId.equals(room.getAuthorId()) && !userId.equals(room.getOwnerId()))) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR);
        }
        return room;
    }

    public LostItemChatRoomInfoEntity validateAccess(Integer articleId, Integer roomId, Integer userId) {
        var room = validateParticipant(articleId, roomId, userId);
        validateNotBlocked(userId, userId.equals(room.getAuthorId()) ? room.getOwnerId() : room.getAuthorId());
        return room;
    }

    public void validateNotBlocked(Integer userId, Integer counterpartId) {
        boolean blockedByMe = blocks.findByBlockerUserAndBlockedUserAndIsActive(userId, counterpartId, true).isPresent();
        boolean blockedByOther = blocks.findByBlockerUserAndBlockedUserAndIsActive(counterpartId, userId, true).isPresent();
        if (blockedByMe || blockedByOther) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_BLOCKED_USER, "차단된 사용자입니다.");
        }
    }

}
