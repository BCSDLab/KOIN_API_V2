package in.koreatech.koin.domain.callvan.service;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.event.CallvanNewMessageEvent;

import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageRequest;
import in.koreatech.koin.domain.callvan.dto.CallvanChatMessageResponse;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanMessageType;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
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

    public CallvanChatMessageResponse getMessages(Integer postId, Integer userId) {
        if (!callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }

        CallvanPost callvanPost = callvanPostRepository.getById(postId);

        List<CallvanChatMessage> messages = callvanChatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(
            callvanPost.getChatRoom().getId());

        return CallvanChatMessageResponse.of(callvanPost, messages, userId);
    }

    @Transactional
    public void sendMessage(Integer postId, Integer userId, CallvanChatMessageRequest request) {
        if (!callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }

        CallvanPost callvanPost = callvanPostRepository.getById(postId);
        User sender = userRepository.getById(userId);

        CallvanChatMessage message = CallvanChatMessage.builder()
            .chatRoom(callvanPost.getChatRoom())
            .sender(sender)
            .senderNickname(getNickname(sender))
            .content(request.content())
            .messageType(request.isImage() ? CallvanMessageType.IMAGE : CallvanMessageType.TEXT)
            .isImage(request.isImage())
            .build();

        callvanChatMessageRepository.save(message);

        eventPublisher.publishEvent(
            new CallvanNewMessageEvent(callvanPost.getId(), message.getSenderNickname(), sender.getId(), message.getContent()));
    }

    private String getNickname(User user) {
        if (user.getNickname() != null) {
            return user.getNickname();
        }
        if (user.getAnonymousNickname() != null) {
            return user.getAnonymousNickname();
        }
        return "익명_" + RandomStringUtils.randomAlphabetic(13);
    }
}
