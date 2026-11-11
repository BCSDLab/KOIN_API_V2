package in.koreatech.koin.domain.callvan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.persistence.LockModeType;

public interface CallvanPostRepository extends Repository<CallvanPost, Integer> {

    CallvanPost save(CallvanPost callvanPost);

    Optional<CallvanPost> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT post FROM CallvanPost post WHERE post.id = :id")
    Optional<CallvanPost> findByIdWithLock(@Param("id") Integer id);

    default CallvanPost getByIdWithLock(Integer postId) {
        return findByIdWithLock(postId).orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE));
    }

    default CallvanPost getById(Integer postId) {
        return findById(postId).orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE));
    }

    @EntityGraph(attributePaths = { "participants", "participants.member" })
    Optional<CallvanPost> findWithParticipantsById(Integer id);

    default CallvanPost getWithParticipantsById(Integer postId) {
        return findWithParticipantsById(postId)
                .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_ARTICLE));
    }
}
