package in.koreatech.koin.domain.callvan.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageResponse;
import in.koreatech.koin.domain.callvan.event.CallvanNewMessageEvent;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.chat.event.ChatMessageStoredEvent;
import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanChatService {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final CallvanChatMessageRepository callvanChatMessageRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public CallvanPost getAccessiblePost(Integer userId, Integer postId, Integer roomId) {
        var participant = callvanParticipantRepository.findActiveWithChatRoomByPostIdAndMemberId(postId, userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT));
        CallvanPost post = participant.getPost();
        if (Boolean.TRUE.equals(participant.getIsDeleted()) || post == null
            || Boolean.TRUE.equals(post.getIsDeleted()) || post.getChatRoom() == null
            || Boolean.TRUE.equals(post.getChatRoom().getIsDeleted())
            || !postId.equals(post.getId()) || !roomId.equals(post.getChatRoom().getId())) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }
        return post;
    }

    @Transactional
    public void markRead(Integer userId, Integer postId, Integer roomId, Long messageId) {
        if (messageId == null || messageId < 1 || messageId > Integer.MAX_VALUE) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        // Serialize against join/leave and send using the existing post lock.
        callvanPostRepository.getByIdWithLock(postId);
        getAccessiblePost(userId, postId, roomId);
        if (!callvanChatMessageRepository.existsByIdAndChatRoomId(messageId.intValue(), roomId)) {
            throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT);
        }
        if (callvanParticipantRepository.advanceReadCursor(postId, userId, messageId.intValue()) > 0) {
            eventPublisher.publishEvent(new ChatRoomChangedEvent(
                new ChatRoomKey(ChatDomainType.CALLVAN, postId, roomId)));
        }
    }

    public CallvanChatMessageResponse getMessages(Integer postId, Integer userId, SupportedLanguage language) {
        if (!callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }

        CallvanPost callvanPost = callvanPostRepository.getById(postId);

        List<CallvanChatMessage> messages = callvanChatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(
            callvanPost.getChatRoom().getId());

        return CallvanChatMessageResponse.of(callvanPost, messages, userId, language);
    }

    @Transactional
    public void sendMessage(Integer postId, Integer userId, CallvanChatMessageRequest request) {
        sendMessageAndGet(postId, userId, request);
    }

    @Transactional
    public CallvanChatMessage sendMessageAndGet(Integer postId, Integer userId, CallvanChatMessageRequest request) {
        // Lock before any consistent read so REPEATABLE_READ sees a committed leave.
        CallvanPost callvanPost = callvanPostRepository.findByIdWithLock(postId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT));
        if (!callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }

        User sender = userRepository.getById(userId);

        CallvanChatMessage message = CallvanChatMessage.builder()
            .chatRoom(callvanPost.getChatRoom())
            .sender(sender)
            .senderNickname(sender.getDisplayNickname())
            .content(request.content())
            .messageType(request.isImage() ? CallvanMessageType.IMAGE : CallvanMessageType.TEXT)
            .isImage(request.isImage())
            .build();

        CallvanChatMessage savedMessage = callvanChatMessageRepository.save(message);
        // TIMESTAMP 컬럼의 실제 저장 정밀도를 응답과 실시간 이벤트에도 사용한다.
        entityManager.refresh(savedMessage);

        eventPublisher.publishEvent(
            new CallvanNewMessageEvent(callvanPost.getId(), message.getSenderNickname(), sender.getId(),
                message.getContent(), message.getMessageType())
        );
        eventPublisher.publishEvent(new ChatMessageStoredEvent(
            new ChatRoomKey(ChatDomainType.CALLVAN, callvanPost.getId(), callvanPost.getChatRoom().getId()),
            in.koreatech.koin.domain.chat.dto.ChatMessageResponse.builder()
                .messageId(savedMessage.getId().toString())
                .userId(savedMessage.getSender() == null ? null : savedMessage.getSender().getId())
                .userNickname(savedMessage.getSenderNickname()).content(savedMessage.getContent())
                .timestamp(savedMessage.getCreatedAt()).isImage(savedMessage.getIsImage())
                .unreadCount(callvanParticipantRepository.countUnreadParticipants(
                    callvanPost.getId(), sender.getId(), savedMessage.getId()))
                .isLeftUser(Boolean.TRUE.equals(savedMessage.getIsLeftUser())).build()));
        return savedMessage;
    }
}
