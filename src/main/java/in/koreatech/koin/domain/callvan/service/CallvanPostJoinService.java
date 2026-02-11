package in.koreatech.koin.domain.callvan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.repository.CallvanChatMessageRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
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

    @Transactional
    @ConcurrencyGuard(lockName = "callvanJoin")
    public void join(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);
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
                .build();

            callvanParticipantRepository.save(participant);
        }
        entityManager.flush();
        updateUserCallvanChatMessage(callvanPost.getChatRoom().getId(), userId, false);
    }

    @Transactional
    public void leave(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);

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
    }

    public void updateUserCallvanChatMessage(Integer postId, Integer userId, Boolean isLeft) {
        callvanChatMessageRepository.updateIsLeftUserByChatRoomIdAndSenderId(
            postId, userId, isLeft
        );
    }
}
