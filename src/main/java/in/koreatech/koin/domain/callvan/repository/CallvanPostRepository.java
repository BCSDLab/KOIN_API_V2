package in.koreatech.koin.domain.callvan.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public interface CallvanPostRepository extends Repository<CallvanPost, Integer> {

    CallvanPost save(CallvanPost callvanPost);

    Optional<CallvanPost> findById(Integer id);

    default CallvanPost getById(Integer postId) {
        return findById(postId).orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE));
    }
}
