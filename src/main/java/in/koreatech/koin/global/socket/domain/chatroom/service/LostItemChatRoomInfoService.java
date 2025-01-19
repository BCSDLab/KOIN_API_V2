package in.koreatech.koin.global.socket.domain.chatroom.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.global.socket.domain.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.global.socket.domain.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.global.socket.domain.chatroom.service.implement.ChatRoomInfoAppender;
import in.koreatech.koin.global.socket.domain.chatroom.service.implement.ChatRoomInfoReader;
import in.koreatech.koin.global.socket.domain.chatroom.service.implement.LostItemArticleReader;
import in.koreatech.koin.global.socket.domain.message.service.implement.MessageReader;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemChatRoomInfoService {

    private final MessageReader messageReader;
    private final ChatRoomInfoReader chatRoomInfoReader;
    private final ChatRoomInfoAppender chatRoomInfoAppender;
    private final LostItemArticleReader lostItemArticleReader;

    public Integer createLostItemChatRoom(Integer articleId, Integer ownerId) {
        var existingChatRoom = chatRoomInfoReader.readByArticleIdAndOwnerId(articleId, ownerId);

        if (existingChatRoom.isPresent()) {
            return existingChatRoom.get().getChatRoomId();
        }

        LostItemArticle lostItemArticle = lostItemArticleReader.readByArticleId(articleId);
        Integer articleAuthorId = lostItemArticle.getAuthor().getId();

        Integer nextChatRoomId = chatRoomInfoReader.getNextChatRoomId(articleId);

        return chatRoomInfoAppender.save(articleId, nextChatRoomId, ownerId, articleAuthorId).getChatRoomId();
    }

    @Transactional
    public List<ChatRoomListResponse> getAllChatRoomInfo(Integer userId) {
        List<LostItemChatRoomInfoEntity> chatRoomInfoList = chatRoomInfoReader.readByUserId(userId);

        if (chatRoomInfoList.isEmpty()) {
            return Collections.emptyList();
        }

        return chatRoomInfoList.stream()
            .map(entity -> {
                var messageSummary = messageReader.getChatRoomSummary(entity.getArticleId(), entity.getChatRoomId(), userId);
                var articleSummary = lostItemArticleReader.getChatRoomSummary(entity.getArticleId());

                if (messageSummary == null) {
                    return null;
                }

                return ChatRoomListResponse.builder()
                    .articleId(entity.getArticleId())
                    .chatRoomId(entity.getChatRoomId())
                    .title(articleSummary.getArticleTitle())
                    .lostItemImageUrl(articleSummary.getItemImage())
                    .recentMessageContent(messageSummary.getLastMessageContent())
                    .unreadMessageCount(messageSummary.getUnreadCount())
                    .lastMessageAt(messageSummary.getLastMessageTime())
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public String getArticleTitle(Integer articleId) {
        return lostItemArticleReader.getArticleTitle(articleId);
    }

    @Transactional
    public String getChatPartnerProfileImage(Integer articleId) {
        return lostItemArticleReader.readByArticleId(articleId).getAuthor().getProfileImageUrl();
    }
}
