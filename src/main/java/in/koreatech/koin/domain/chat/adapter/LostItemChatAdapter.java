package in.koreatech.koin.domain.chat.adapter;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.model.ChatRoomType;
import in.koreatech.koin.domain.chat.service.ChatDomainAdapter;
import in.koreatech.koin.domain.community.lostitem.chatmessage.dto.ChatMessage;
import in.koreatech.koin.domain.community.lostitem.chatmessage.model.ChatMessageEntity;
import in.koreatech.koin.domain.community.lostitem.chatmessage.repository.ChatMessageRedisRepository;
import in.koreatech.koin.domain.community.lostitem.chatmessage.service.ChatMessagePollingService;
import in.koreatech.koin.domain.community.lostitem.chatroom.model.LostItemChatRoomInfoEntity;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatAccessService;
import in.koreatech.koin.domain.community.lostitem.chatroom.service.LostItemChatRoomInfoService;
import in.koreatech.koin.domain.community.lostitem.chatroom.usecase.LostItemChatRoomUseCase;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LostItemChatAdapter implements ChatDomainAdapter {

    private final LostItemChatRoomInfoService roomInfo;
    private final LostItemChatRoomUseCase roomDisplay;
    private final LostItemChatAccessService access;
    private final ChatMessageRedisRepository messages;
    private final ChatMessagePollingService polling;
    private final UserRepository users;

    @Override
    public ChatDomainType domainType() {
        return ChatDomainType.LOST_ITEM;
    }

    @Override
    public List<ChatRoomResponse> getRooms(Integer userId, SupportedLanguage language) {
        // 기존 목록의 양방향 차단, 탈퇴 및 게시글 삭제 필터를 유지한다.
        return roomInfo.getAllChatRoomInfo(userId).stream()
            .map(room -> {
                ChatRoomKey key = new ChatRoomKey(domainType(), room.articleId(), room.chatRoomId());
                LostItemChatRoomInfoEntity entity = authorize(userId, key);
                return roomBuilder(userId, key, entity)
                    .roomName(room.articleTitle())
                    .imageUrl(room.lostItemImageUrl())
                    .unreadMessageCount(room.unreadMessageCount())
                    .lastMessageContent(room.recentMessageContent())
                    .lastMessageAt(room.lastMessageAt())
                    .build();
            })
            .toList();
    }

    @Override
    public ChatRoomResponse getRoom(Integer userId, ChatRoomKey key, SupportedLanguage language) {
        LostItemChatRoomInfoEntity room = authorize(userId, key);
        var display = roomDisplay.getLostItemChatRoom(key.contextId(), userId, key.chatRoomId());
        var summary = roomInfo.getMessageSummary(key.contextId(), key.chatRoomId(), userId);
        return roomBuilder(userId, key, room)
            .roomName(display.articleTitle())
            .imageUrl(roomInfo.getArticleSummary(key.contextId()).lostItemImage())
            .unreadMessageCount(summary == null ? 0 : summary.unreadCount())
            .lastMessageContent(summary == null ? null : summary.lastMessageContent())
            .lastMessageAt(summary == null ? room.getCreatedAt() : summary.lastMessageTime())
            .build();
    }

    @Override
    public List<ChatMessageResponse> getMessages(Integer userId, ChatRoomKey key, ChatMessageQuery query) {
        authorize(userId, key);
        return query.select(messages.findByArticleIdAndChatRoomId(key.contextId(), key.chatRoomId()).stream()
            .map(this::messageResponse)
            .toList());
    }

    @Override
    public ChatMessageResponse sendMessage(Integer userId, ChatRoomKey key, CreateChatMessageRequest request) {
        authorize(userId, key);
        var user = users.getById(userId);
        ChatMessage message = ChatMessage.builder()
            .userId(userId)
            .userNickname(user.getDisplayNickname())
            .content(request.content())
            .timestamp(LocalDateTime.now())
            .isImage(request.isImage())
            .build();
        return messageResponse(polling.sendMessageEntity(key.contextId(), key.chatRoomId(), userId, message));
    }

    @Override
    public void validateReadAccess(Integer userId, ChatRoomKey key) {
        authorize(userId, key);
    }

    @Override
    public void markRead(Integer userId, ChatRoomKey key, Long messageId) {
        authorize(userId, key);
        polling.markRead(key.contextId(), key.chatRoomId(), userId, messageId);
    }

    private LostItemChatRoomInfoEntity authorize(Integer userId, ChatRoomKey key) {
        if (key.domainType() != domainType()) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        return access.validateAccess(key.contextId(), key.chatRoomId(), userId);
    }

    private ChatRoomResponse.ChatRoomResponseBuilder roomBuilder(
        Integer userId, ChatRoomKey key, LostItemChatRoomInfoEntity room
    ) {
        Integer counterpartId = userId.equals(room.getAuthorId()) ? room.getOwnerId() : room.getAuthorId();
        var counterpart = users.findById(counterpartId);
        String profileImage = counterpart.map(user -> user.getProfileImageUrl()).orElse(null);
        return ChatRoomResponse.builder()
            .roomId(key.encoded())
            .domainType(domainType())
            .contextId(key.contextId())
            .chatRoomId(key.chatRoomId())
            .roomType(ChatRoomType.DIRECT)
            .memberCount(2)
            .maxMemberCount(2)
            .counterpartId(counterpartId)
            .counterpartNickname(counterpart.map(user -> user.getDisplayNickname()).orElse(null))
            .counterpartProfileImage(profileImage)
            .canSend(true)
            .supportsReadReceipts(true);
    }

    private ChatMessageResponse messageResponse(ChatMessageEntity message) {
        return ChatMessageResponse.builder()
            .messageId(message.getId().toString())
            .userId(message.getUserId())
            .userNickname(message.getNickName())
            .content(message.getContents())
            .timestamp(message.getCreatedAt())
            .isImage(message.getIsImage())
            .unreadCount(Boolean.TRUE.equals(message.getIsRead()) ? 0 : 1)
            .isLeftUser(false)
            .build();
    }
}
