package in.koreatech.koin.socket.domain.chatroom.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.socket.domain.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.socket.domain.chatroom.exception.SelfChatNotAllowedException;
import in.koreatech.koin.socket.domain.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.socket.domain.chatroom.service.implement.ChatRoomInfoAppender;
import in.koreatech.koin.socket.domain.chatroom.service.implement.ChatRoomInfoReader;
import in.koreatech.koin.socket.domain.chatroom.service.implement.LostItemArticleReader;
import in.koreatech.koin.socket.domain.chatroom.service.implement.UserBlockReader;
import in.koreatech.koin.socket.domain.message.service.implement.MessageReader;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LostItemChatRoomInfoService {

    private final MessageReader messageReader;
    private final ChatRoomInfoReader chatRoomInfoReader;
    private final ChatRoomInfoAppender chatRoomInfoAppender;
    private final LostItemArticleReader lostItemArticleReader;
    private final UserBlockReader userBlockReader;
    private static final String DEFAULT_MESSAGE = "대화를 시작해보세요!";

    public Integer createLostItemChatRoom(Integer articleId, Integer ownerId) {
        var existingChatRoom = chatRoomInfoReader.readByArticleIdAndOwnerId(articleId, ownerId);

        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get().getChatRoomId();
        }

        LostItemArticle lostItemArticle = lostItemArticleReader.readByArticleId(articleId);
        User author = lostItemArticle.getAuthor();
        if (author == null) {
            throw UserNotFoundException.withDetail("탈퇴한 사용자입니다.");
        }
        Integer articleAuthorId = author.getId();

        checkSelfChat(ownerId, articleAuthorId);

        Integer nextChatRoomId = chatRoomInfoReader.getNextChatRoomId(articleId);

        chatRoomInfoAppender.save(articleId, nextChatRoomId, ownerId, articleAuthorId);

        return nextChatRoomId;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getAllChatRoomInfo(Integer userId) {
        List<LostItemChatRoomInfoEntity> chatRoomInfoList = chatRoomInfoReader.readByUserId(userId);

        if (chatRoomInfoList.isEmpty()) {
            return Collections.emptyList();
        }

        return chatRoomInfoList.stream()
            .flatMap(entity -> {
                var articleSummary = lostItemArticleReader.getArticleSummary(entity.getArticleId());
                if (articleSummary == null || isUserBlocked(entity.getArticleId(), entity.getChatRoomId(), userId)) {
                    return Stream.empty();
                }

                var messageSummary = messageReader.getMessageSummary(entity.getArticleId(), entity.getChatRoomId(), userId);
                var responseBuilder = ChatRoomListResponse.builder()
                    .articleId(entity.getArticleId())
                    .chatRoomId(entity.getChatRoomId())
                    .articleTitle(articleSummary.getArticleTitle())
                    .lostItemImageUrl(articleSummary.getItemImage());

                if (messageSummary == null && entity.getAuthorId().equals(userId)) {
                    return Stream.empty();
                }

                if(messageSummary == null) {
                    responseBuilder
                        .recentMessageContent(DEFAULT_MESSAGE)
                        .unreadMessageCount(0)
                        .lastMessageAt(entity.getCreatedAt());
                } else {
                    responseBuilder
                        .recentMessageContent(messageSummary.getLastMessageContent())
                        .unreadMessageCount(messageSummary.getUnreadCount())
                        .lastMessageAt(messageSummary.getLastMessageTime());
                }
                return Stream.of(responseBuilder.build());
            })
            .sorted(Comparator.comparing(ChatRoomListResponse::lastMessageAt, Comparator.reverseOrder()))
            .toList();
    }

    public String getArticleTitle(Integer articleId) {
        return lostItemArticleReader.getArticleTitle(articleId);
    }

    @Transactional(readOnly = true)
    public String getChatPartnerProfileImage(Integer articleId) {
        return lostItemArticleReader.readByArticleId(articleId).getAuthor().getProfileImageUrl();
    }

    private void checkSelfChat(Integer ownerId, Integer articleAuthorId) {
        if (ownerId.equals(articleAuthorId)) {
            throw new SelfChatNotAllowedException("사용자가 자신과 채팅방을 생성할 수 없습니다.");
        }
    }

    private boolean isUserBlocked(Integer articleId, Integer chatRoomId, Integer studentId) {
        LostItemChatRoomInfoEntity chatRoomInfo = chatRoomInfoReader.readByArticleIdAndChatRoomId(articleId, chatRoomId);
        Integer articleAuthorId = chatRoomInfo.getAuthorId();
        Integer ownerId = chatRoomInfo.getOwnerId();

        Integer otherUserId;
        if (studentId.equals(articleAuthorId)) {
            otherUserId = ownerId;
        } else {
            otherUserId = articleAuthorId;
        }

        boolean blockedByMe = userBlockReader.readByBlockerUserIdAndBlockedUserIdAndIsActive(
            studentId, otherUserId, true).isPresent();
        boolean blockedByOther = userBlockReader.readByBlockerUserIdAndBlockedUserIdAndIsActive(
            otherUserId, studentId, true).isPresent();

        return blockedByMe || blockedByOther;
    }
}
