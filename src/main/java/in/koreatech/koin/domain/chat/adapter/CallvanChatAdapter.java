package in.koreatech.koin.domain.chat.adapter;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageResponse;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.service.CallvanChatService;
import in.koreatech.koin.domain.chat.dto.ChatMessageQuery;
import in.koreatech.koin.domain.chat.dto.ChatMessageResponse;
import in.koreatech.koin.domain.chat.dto.ChatRoomResponse;
import in.koreatech.koin.domain.chat.dto.CreateChatMessageRequest;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.chat.model.ChatRoomType;
import in.koreatech.koin.domain.chat.service.ChatDomainAdapter;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanChatAdapter implements ChatDomainAdapter {

    private final CallvanParticipantRepository participantRepository;
    private final CallvanChatMessageRepository messageRepository;
    private final CallvanChatService chatService;

    @Override
    public ChatDomainType domainType() {
        return ChatDomainType.CALLVAN;
    }

    @Override
    public List<ChatRoomResponse> getRooms(Integer userId, SupportedLanguage language) {
        return participantRepository.findActiveWithChatRoomByMemberId(userId).stream()
            .filter(this::hasActiveRoom)
            .map(participant -> toRoom(participant.getPost(), userId, language))
            .toList();
    }

    @Override
    public ChatRoomResponse getRoom(Integer userId, ChatRoomKey key, SupportedLanguage language) {
        return toRoom(requirePost(userId, key), userId, language);
    }

    @Override
    public List<ChatMessageResponse> getMessages(Integer userId, ChatRoomKey key, ChatMessageQuery query) {
        CallvanPost post = requirePost(userId, key);
        Integer roomId = post.getChatRoom().getId();
        PageRequest page = PageRequest.of(0, query.limit());
        List<CallvanChatMessage> messages;
        if (query.afterMessageId() != null) {
            messages = messageRepository.findByChatRoomIdAndIdGreaterThanOrderByIdAsc(
                roomId, query.afterAsInteger(), page);
        } else if (query.beforeMessageId() != null) {
            messages = messageRepository.findByChatRoomIdAndIdLessThanOrderByIdDesc(
                roomId, query.beforeAsInteger(), page);
        } else {
            messages = messageRepository.findByChatRoomIdOrderByIdDesc(roomId, page);
        }
        List<CallvanParticipantRepository.ReadCursor> readers =
            participantRepository.findActiveReadCursorsByPostId(post.getId());
        return messages.stream().sorted(Comparator.comparing(CallvanChatMessage::getId))
            .map(message -> {
                int unreadCount = (int) readers.stream()
                    .filter(reader -> message.getSender() == null
                        || !reader.getMemberId().equals(message.getSender().getId()))
                    .filter(reader -> reader.getLastReadMessageId() < message.getId())
                    .count();
                return toMessage(message, unreadCount);
            }).toList();
    }

    @Override
    // Start the save transaction after preflight so membership is rechecked after the post lock.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ChatMessageResponse sendMessage(Integer userId, ChatRoomKey key, CreateChatMessageRequest request) {
        CallvanPost post = requirePost(userId, key);
        CallvanChatMessage message = chatService.sendMessageAndGet(post.getId(), userId,
            new CallvanChatMessageRequest(request.content(), request.isImage()));
        int unreadCount = participantRepository.countUnreadParticipants(post.getId(),
            message.getSender() == null ? null : message.getSender().getId(), message.getId());
        return toMessage(message, unreadCount);
    }

    @Override
    public void validateReadAccess(Integer userId, ChatRoomKey key) {
        requirePost(userId, key);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void markRead(Integer userId, ChatRoomKey key, Long messageId) {
        validateReadAccess(userId, key);
        chatService.markRead(userId, key.contextId(), key.chatRoomId(), messageId);
    }

    private CallvanPost requirePost(Integer userId, ChatRoomKey key) {
        if (key.domainType() != domainType()) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }
        return chatService.getAccessiblePost(userId, key.contextId(), key.chatRoomId());
    }

    private boolean hasActiveRoom(CallvanParticipant participant) {
        CallvanPost post = participant.getPost();
        return !Boolean.TRUE.equals(participant.getIsDeleted())
            && post != null && !Boolean.TRUE.equals(post.getIsDeleted())
            && post.getChatRoom() != null && !Boolean.TRUE.equals(post.getChatRoom().getIsDeleted());
    }

    private ChatRoomResponse toRoom(CallvanPost post, Integer userId, SupportedLanguage language) {
        Integer roomId = post.getChatRoom().getId();
        CallvanChatMessage last = messageRepository.findFirstByChatRoomIdOrderByIdDesc(roomId).orElse(null);
        return ChatRoomResponse.builder()
            .roomId(new ChatRoomKey(domainType(), post.getId(), roomId).encoded())
            .domainType(domainType()).contextId(post.getId()).chatRoomId(roomId)
            .roomType(ChatRoomType.GROUP)
            .roomName(CallvanChatMessageResponse.of(post, List.of(), userId, language).roomName())
            .memberCount(post.getCurrentParticipants()).maxMemberCount(post.getMaxParticipants())
            .canSend(true).supportsReadReceipts(true)
            .unreadMessageCount(messageRepository.countUnreadMessages(roomId, userId))
            .lastMessageId(last == null ? null : last.getId().toString())
            .lastMessageIsImage(last == null ? null : last.getIsImage())
            .lastMessageContent(last == null ? null : last.getContent())
            .lastMessageAt(last == null ? null : last.getCreatedAt())
            .build();
    }

    private ChatMessageResponse toMessage(CallvanChatMessage message, int unreadCount) {
        return ChatMessageResponse.builder()
            .messageId(message.getId().toString()).userId(message.getSender() == null ? null : message.getSender().getId())
            .userNickname(message.getSenderNickname()).content(message.getContent())
            .timestamp(message.getCreatedAt()).isImage(message.getIsImage())
            .unreadCount(unreadCount).isLeftUser(Boolean.TRUE.equals(message.getIsLeftUser()))
            .build();
    }
}
