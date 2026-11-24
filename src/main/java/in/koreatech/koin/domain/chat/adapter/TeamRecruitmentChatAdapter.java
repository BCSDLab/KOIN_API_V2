package in.koreatech.koin.domain.chat.adapter;

import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_CHAT_NOT_FOUND;

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
import in.koreatech.koin.domain.teamrecruitment.dto.TeamRecruitmentChatRoomListItemResponse;
import in.koreatech.koin.domain.teamrecruitment.service.TeamRecruitmentChatService;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamRecruitmentChatAdapter implements ChatDomainAdapter {

    private final TeamRecruitmentChatService chatService;

    @Override
    public ChatDomainType domainType() {
        return ChatDomainType.TEAM_RECRUITMENT;
    }

    @Override
    public List<ChatRoomResponse> getRooms(Integer userId, SupportedLanguage language) {
        return chatService.getChatRoomsWithSendPermission(userId).stream()
            .map(item -> roomResponse(item.response()).canSend(item.canSend()).build())
            .toList();
    }

    @Override
    public ChatRoomResponse getRoom(Integer userId, ChatRoomKey key, SupportedLanguage language) {
        var detail = chatService.getChatRoom(userId, key.contextId(), key.chatRoomId());
        var room = chatService.getChatRooms(userId).stream()
            .filter(item -> item.recruitmentId().equals(key.contextId())
                && item.chatRoomId().equals(key.chatRoomId()))
            .findFirst()
            .orElseThrow(() -> CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND));
        return roomResponse(room)
            .canSend(chatService.canSend(userId, key.contextId(), key.chatRoomId()))
            .memberCount(detail.memberCount())
            .maxMemberCount(detail.maxMemberCount())
            .build();
    }

    @Override
    public List<ChatMessageResponse> getMessages(Integer userId, ChatRoomKey key, ChatMessageQuery query) {
        return chatService.getMessagesWithoutMarkingRead(userId, key.contextId(), key.chatRoomId(),
                query.afterAsInteger(), query.beforeAsInteger(), query.limit()).stream()
            .map(this::messageResponse)
            .toList();
    }

    @Override
    public ChatMessageResponse sendMessage(Integer userId, ChatRoomKey key, CreateChatMessageRequest request) {
        var domainRequest = new in.koreatech.koin.domain.teamrecruitment.dto.CreateChatMessageRequest(
            request.content(), request.isImage());
        return messageResponse(chatService.createMessageWithoutMarkingRead(userId, key.contextId(), key.chatRoomId(), domainRequest));
    }

    @Override
    public void validateReadAccess(Integer userId, ChatRoomKey key) {
        if (key.domainType() != domainType()) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND);
        }
        chatService.validateReadAccess(userId, key.contextId(), key.chatRoomId());
    }

    @Override
    public void markRead(Integer userId, ChatRoomKey key, Long messageId) {
        if (key.domainType() != domainType()) {
            throw CustomException.of(TEAM_RECRUITMENT_CHAT_NOT_FOUND);
        }
        chatService.markRead(userId, key.contextId(), key.chatRoomId(), messageId);
    }

    private ChatRoomResponse.ChatRoomResponseBuilder roomResponse(TeamRecruitmentChatRoomListItemResponse room) {
        return ChatRoomResponse.builder()
            .roomId(new ChatRoomKey(domainType(), room.recruitmentId(), room.chatRoomId()).encoded())
            .supportsReadReceipts(true)
            .domainType(domainType())
            .contextId(room.recruitmentId())
            .chatRoomId(room.chatRoomId())
            .roomType("TEAM".equals(room.roomType()) ? ChatRoomType.GROUP : ChatRoomType.DIRECT)
            .roomName(room.roomName())
            .counterpartId(room.counterpartId())
            .counterpartNickname(room.counterpartNickname())
            .unreadMessageCount(room.unreadMessageCount())
            .lastMessageId(room.lastMessageId() == null ? null : room.lastMessageId().toString())
            .lastMessageIsImage(room.lastMessageIsImage())
            .lastMessageContent(room.lastMessageContent())
            .lastMessageAt(room.lastMessageAt());
    }

    private ChatMessageResponse messageResponse(
        in.koreatech.koin.domain.teamrecruitment.dto.ChatMessageResponse message
    ) {
        return ChatMessageResponse.builder()
            .messageId(message.messageId().toString())
            .userId(message.userId())
            .userNickname(message.userNickname())
            .content(message.content())
            .timestamp(message.timestamp())
            .isImage(message.isImage())
            .unreadCount(message.unreadCount())
            // Team withdrawal preserves the sender snapshot; userId=null identifies the withdrawn account.
            // isLeftUser is reserved for leaving a Callvan pot.
            .isLeftUser(false)
            .build();
    }
}
