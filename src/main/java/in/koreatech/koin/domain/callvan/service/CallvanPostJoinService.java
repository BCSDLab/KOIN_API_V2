package in.koreatech.koin.domain.callvan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.model.CallvanParticipant;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.repository.CallvanParticipantRepository;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanPostJoinService {

    private final CallvanPostRepository callvanPostRepository;
    private final CallvanParticipantRepository callvanParticipantRepository;
    private final UserRepository userRepository;

    @Transactional
    @ConcurrencyGuard(lockName = "callvanJoin")
    public void join(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);
        User user = userRepository.getById(userId);

        if (callvanParticipantRepository.existsByPostIdAndMemberId(postId, userId)) {
            throw CustomException.of(ApiResponseCode.CALLVAN_ALREADY_JOINED);
        }

        callvanPost.checkJoinable();
        callvanPost.increaseParticipantCount();

        CallvanParticipant participant = CallvanParticipant.builder()
            .post(callvanPost)
            .member(user)
            .build();

        callvanParticipantRepository.save(participant);
    }
}
