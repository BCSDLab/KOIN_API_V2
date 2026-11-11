package in.koreatech.koin.domain.callvan.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.event.CallvanParticipantJoinedEvent;
import in.koreatech.koin.domain.callvan.event.CallvanRecruitmentCompletedEvent;
import in.koreatech.koin.domain.callvan.model.CallvanChatMessage;
import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.chat.event.ChatRoomChangedEvent;
import in.koreatech.koin.domain.chat.model.ChatDomainType;
import in.koreatech.koin.domain.chat.model.ChatRoomKey;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CallvanPostJoinService {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final CallvanChatMessageRepository callvanChatMessageRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;
    private final CallvanRestrictionService callvanRestrictionService;

    @Transactional
    @ConcurrencyGuard(lockName = "callvanJoin")
    public void join(Integer postId, Integer userId) {
        // 잠금 대기 중 커밋된 메시지도 읽도록 일반 조회보다 게시글 잠금을 먼저 획득한다.
        Optional<CallvanPost> post = callvanPostRepository.findByIdWithLock(postId);
        callvanRestrictionService.validateNotRestricted(userId);
        CallvanPost callvanPost = post.orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE));
        User user = userRepository.getById(userId);

        if (callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, userId)) {
            throw CustomException.of(ApiResponseCode.CALLVAN_ALREADY_JOINED);
        }

        callvanPost.checkJoinable();
        callvanPost.increaseParticipantCount();

        if (callvanParticipantRepository.existsByPostIdAndMemberIdAndIsDeletedTrue(postId, userId)) {
            CallvanParticipant callvanParticipant = callvanParticipantRepository.findByPostIdAndMemberId(postId, userId)
                .orElseThrow(() -> CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT));
            callvanParticipant.joinCallvanAgain();
        } else {
            CallvanParticipant participant = CallvanParticipant.builder()
                .post(callvanPost)
                .member(user)
                .lastReadMessageId(callvanChatMessageRepository
                    .findFirstByChatRoomIdOrderByIdDesc(callvanPost.getChatRoom().getId())
                    .map(CallvanChatMessage::getId).orElse(0))
                .build();

            callvanParticipantRepository.save(participant);
        }
        entityManager.flush();
        updateUserCallvanChatMessage(callvanPost.getChatRoom().getId(), userId, false);
        publishRoomChanged(callvanPost);

        eventPublisher.publishEvent(new CallvanParticipantJoinedEvent(callvanPost.getId(), user.getId(), user.getDisplayNickname()));

        if (callvanPost.getCurrentParticipants() >= callvanPost.getMaxParticipants()) {
            eventPublisher.publishEvent(new CallvanRecruitmentCompletedEvent(callvanPost.getId()));
        }
    }

    @Transactional
    public void leave(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getByIdWithLock(postId);

        if (callvanPost.getAuthor().getId().equals(userId)) {
            throw CustomException.of(ApiResponseCode.CALLVAN_POST_AUTHOR);
        }

        CallvanParticipant participant = callvanParticipantRepository.findByPostIdAndMemberId(postId, userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT));

        if (participant.getIsDeleted()) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_PARTICIPANT);
        }

        callvanPost.decreaseParticipantCount();
        participant.leaveCallvan();
        entityManager.flush();
        updateUserCallvanChatMessage(callvanPost.getChatRoom().getId(), userId, true);
        publishRoomChanged(callvanPost);
    }

    private void publishRoomChanged(CallvanPost post) {
        eventPublisher.publishEvent(new ChatRoomChangedEvent(
            new ChatRoomKey(ChatDomainType.CALLVAN, post.getId(), post.getChatRoom().getId())));
    }

    public void updateUserCallvanChatMessage(Integer postId, Integer userId, Boolean isLeft) {
        callvanChatMessageRepository.updateIsLeftUserByChatRoomIdAndSenderId(
            postId, userId, isLeft
        );
    }
}
