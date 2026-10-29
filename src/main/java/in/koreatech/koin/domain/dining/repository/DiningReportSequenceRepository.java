package in.koreatech.koin.domain.dining.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.DiningReportSequence;
import jakarta.persistence.LockModeType;

public interface DiningReportSequenceRepository extends Repository<DiningReportSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DiningReportSequence s WHERE s.id = 1")
    DiningReportSequence findForUpdate();

    Optional<DiningReportSequence> findById(Integer id);
}
