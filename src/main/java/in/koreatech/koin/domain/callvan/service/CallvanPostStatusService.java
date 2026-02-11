package in.koreatech.koin.domain.callvan.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.callvan.event.CallvanRecruitmentCompletedEvent;

import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.repository.CallvanPostRepository;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CallvanPostStatusService {

    private final CallvanPostRepository callvanPostRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void close(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);

        if (!callvanPost.getAuthor().getId().equals(userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR);
        }

        callvanPost.closeRecruitment();
        eventPublisher.publishEvent(new CallvanRecruitmentCompletedEvent(callvanPost.getId()));
    }

    @Transactional
    public void reopen(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);

        if (!callvanPost.getAuthor().getId().equals(userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR);
        }

        callvanPost.reopenRecruitment();
    }

    @Transactional
    public void complete(Integer postId, Integer userId) {
        CallvanPost callvanPost = callvanPostRepository.getById(postId);

        if (!callvanPost.getAuthor().getId().equals(userId)) {
            throw CustomException.of(ApiResponseCode.FORBIDDEN_AUTHOR);
        }

        callvanPost.completeRecruitment();
    }
}
