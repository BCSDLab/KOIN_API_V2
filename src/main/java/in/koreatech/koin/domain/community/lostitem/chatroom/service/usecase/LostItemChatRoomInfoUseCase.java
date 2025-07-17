package in.koreatech.koin.domain.community.lostitem.chatroom.service.usecase;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin.domain.community.article.dto.LostItemArticleSummary;
import in.koreatech.koin.domain.community.article.model.LostItemArticle;
import in.koreatech.koin.domain.community.article.repository.LostItemArticleRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatRoomInfoRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.repository.LostItemChatUserBlockRepository;
import in.koreatech.koin.domain.community.lostitem.chatroom.dto.ChatRoomListResponse;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
public class LostItemChatRoomInfoUseCase {

    private final LostItemArticleRepository lostItemArticleRepository;
    private final LostItemChatRoomInfoRepository chatRoomInfoRepository;
    private final LostItemChatUserBlockRepository userBlockRepository;
    private final ChatMessageRedisRepository chatMessageRepository;
    private final UserRepository userRepository;

    private static final String DEFAULT_MESSAGE = "대화를 시작해보세요!";

    public Integer getOrCreateChatRoomId(Integer articleId, Integer messageSendUserId) {
        var existChatRoom = findAlreadyCreatedChatRoom(articleId, messageSendUserId);
        if(existChatRoom.isPresent()) {
            return existChatRoom.get().getChatRoomId();
        } else {
            return createChatRoomAndGetId(articleId, messageSendUserId);
        }
    }

    public List<ChatRoomListResponse> getAllChatRoomInfo(Integer userId) {
        List<LostItemChatRoomInfoEntity> chatRoomInfoList = chatRoomInfoRepository.findByUserId(userId);

        if (chatRoomInfoList.isEmpty()) {
            return Collections.emptyList();
        }

        return chatRoomInfoList.stream()
            .flatMap(entity -> {
                if (!userRepository.existsById(entity.getOwnerId())) {
                    return Stream.empty();
                }

                var articleSummary = getArticleSummary(entity.getArticleId());
                if (articleSummary == null || isUserBlocked(entity.getArticleId(), entity.getChatRoomId(), userId)) {
                    return Stream.empty();
                }

                var messageSummary = getMessageSummary(entity.getArticleId(), entity.getChatRoomId(), userId);
                var responseBuilder = ChatRoomListResponse.builder()
                    .articleId(entity.getArticleId())
                    .chatRoomId(entity.getChatRoomId())
                    .articleTitle(articleSummary.articleTitle())
                    .lostItemImageUrl(articleSummary.lostItemImage());

                if (messageSummary == null && entity.getAuthorId().equals(userId)) {
                    return Stream.empty();
                }

                if (messageSummary == null) {
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

    /* ======= 헬퍼 메소드 ======= */

    private Integer createChatRoomAndGetId(Integer articleId, Integer messageSendUserId) {
        LostItemArticle lostItemArticle = lostItemArticleRepository.getByArticleId(articleId);
        validateAuthor(lostItemArticle.getAuthor());
        validateSelfChat(messageSendUserId, lostItemArticle.getAuthor().getId());

        Integer nextChatRoomId = getNextChatRoomId(lostItemArticle.getId());
        chatRoomInfoRepository.save(
            LostItemChatRoomInfoEntity.toEntity(
                articleId, nextChatRoomId, messageSendUserId, lostItemArticle.getAuthor().getId()
            )
        );
        return nextChatRoomId;
    }

    private Optional<LostItemChatRoomInfoEntity> findAlreadyCreatedChatRoom(
        Integer lostItemArticleId, Integer messageSendUserId
    ) {
        return chatRoomInfoRepository.findByArticleIdAndMessageSenderId(lostItemArticleId, messageSendUserId);
    }

    public LostItemArticleSummary getArticleSummary(Integer articleId) {
        return lostItemArticleRepository.getArticleSummary(articleId);
    }

    private void validateAuthor(User user) {
        if (user == null) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_USER, "탈퇴한 사용자입니다.");
        }
    }

    private void validateSelfChat(Integer messageSenderId, Integer articleAuthorId) {
        if (messageSenderId.equals(articleAuthorId)) {
            throw CustomException.of(ApiResponseCode.INVALID_SELF_CHAT, "사용자가 자신과 채팅방을 생성할 수 없습니다.");
        }
    }

    private Integer getNextChatRoomId(Integer articleId) {
        List<LostItemChatRoomInfoEntity> chatRooms = chatRoomInfoRepository.findByArticleId(articleId);

        if (chatRooms.isEmpty()) {
            return 1;
        }

        return chatRooms.stream()
            .mapToInt(LostItemChatRoomInfoEntity::getChatRoomId)
            .max()
            .orElse(0) + 1;
    }

    private boolean isUserBlocked(Integer articleId, Integer chatRoomId, Integer studentId) {
        LostItemChatRoomInfoEntity chatRoomInfo = chatRoomInfoRepository.getByArticleIdAndChatRoomId(articleId, chatRoomId);
        Integer articleAuthorId = chatRoomInfo.getAuthorId();
        Integer ownerId = chatRoomInfo.getOwnerId();

        Integer otherUserId;
        if (studentId.equals(articleAuthorId)) {
            otherUserId = ownerId;
        } else {
            otherUserId = articleAuthorId;
        }

        boolean blockedByMe = userBlockRepository.findByBlockerUserAndBlockedUserAndIsActive(
            studentId, otherUserId, true).isPresent();
        boolean blockedByOther = userBlockRepository.findByBlockerUserAndBlockedUserAndIsActive(
            otherUserId, studentId, true).isPresent();

        return blockedByMe || blockedByOther;
    }

    public MessageSummary getMessageSummary(Integer articleId, Integer chatRoomId, Integer userId) {
        List<ChatMessageEntity> messages = chatMessageRepository.findByArticleIdAndChatRoomId(articleId, chatRoomId);

        if (messages.isEmpty()) {
            return null;
        }

        // 읽지 않은 메시지 수 계산
        int unreadCount = (int) messages.stream()
            .filter(message -> !message.getUserId().equals(userId)) // 상대방이 보낸 메시지
            .filter(message -> !message.getIsRead()) // 읽지 않은 메시지
            .count();

        // 최근 메시지 내용 조회
        String lastMessageContent = messages.stream()
            .max(Comparator.comparing(ChatMessageEntity::getId))
            .map(message -> message.getIsImage() ? "사진" : message.getContents())
            .orElse("");

        //최근 메시지 시간 조회
        LocalDateTime lastMessageTime = messages.stream()
            .max(Comparator.comparing(ChatMessageEntity::getId))
            .map(ChatMessageEntity::getCreatedAt)
            .orElse(null);

        return MessageSummary.builder()
            .unreadCount(unreadCount)
            .lastMessageContent(lastMessageContent)
            .lastMessageTime(lastMessageTime)
            .build();
    }

    @Getter
    @Builder
    public static class MessageSummary {
        private Integer unreadCount;
        private String lastMessageContent;
        private LocalDateTime lastMessageTime;
    }
}
