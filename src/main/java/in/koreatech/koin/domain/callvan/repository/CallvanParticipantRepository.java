package in.koreatech.koin.domain.callvan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanParticipant;

public interface CallvanParticipantRepository extends Repository<CallvanParticipant, Integer> {

    CallvanParticipant save(CallvanParticipant callvanParticipant);

    boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Integer postId, Integer memberId);

    boolean existsByPostIdAndMemberIdAndIsDeletedTrue(Integer postId, Integer memberId);

    Optional<CallvanParticipant> findByPostIdAndMemberId(Integer postId, Integer memberId);

    void delete(CallvanParticipant callvanParticipant);

    List<CallvanParticipant> findAllByMemberIdAndPostIdIn(Integer memberId, List<Integer> postIds);
}
