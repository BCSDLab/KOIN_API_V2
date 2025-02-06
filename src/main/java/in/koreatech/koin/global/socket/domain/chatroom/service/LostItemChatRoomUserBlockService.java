package in.koreatech.koin.global.socket.domain.chatroom.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.global.socket.domain.chatroom.exception.UserBlockException;
import in.koreatech.koin.global.socket.domain.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.global.socket.domain.chatroom.service.implement.ChatRoomInfoReader;
import in.koreatech.koin.global.socket.domain.chatroom.service.implement.UserBlockAppender;
import in.koreatech.koin.global.socket.domain.chatroom.service.implement.UserBlockReader;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemChatRoomUserBlockService {

    private final ChatRoomInfoReader chatRoomInfoReader;
    private final UserBlockReader userBlockReader;
    private final UserBlockAppender userBlockAppender;

    @Transactional
    public void blockUser(Integer articleId, Integer chatRoomId, Integer userId) {
        Integer otherUserId = findOtherUserId(articleId, chatRoomId, userId);
        userBlockAppender.save(userId, otherUserId);
    }

    public void checkUserBlock(Integer articleId, Integer chatRoomId, Integer userId) {
        Integer otherUserId = findOtherUserId(articleId, chatRoomId, userId);

        boolean blockedByMe = userBlockReader.readByBlockerUserIdAndBlockedUserId(userId, otherUserId)
            .isPresent();
        boolean blockedByOther = userBlockReader.readByBlockerUserIdAndBlockedUserId(otherUserId, userId)
            .isPresent();

        if (blockedByMe || blockedByOther) {
            throw new UserBlockException("차단된 사용자 입니다.");
        }
    }

    private Integer findOtherUserId(Integer articleId, Integer chatRoomId, Integer userId) {
        LostItemChatRoomInfoEntity chatRoomInfo = chatRoomInfoReader.readByArticleIdAndChatRoomId(
            articleId, chatRoomId
        );

        Integer articleAuthorId = chatRoomInfo.getAuthorId();
        Integer ownerId = chatRoomInfo.getOwnerId();

        return userId.equals(articleAuthorId) ? ownerId : articleAuthorId;
    }
}
