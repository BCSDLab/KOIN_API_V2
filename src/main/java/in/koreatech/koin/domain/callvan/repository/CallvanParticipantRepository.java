package in.koreatech.koin.domain.callvan.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.callvan.model.CallvanParticipant;

public interface CallvanParticipantRepository extends Repository<CallvanParticipant, Integer> {

    CallvanParticipant save(CallvanParticipant callvanParticipant);
}
