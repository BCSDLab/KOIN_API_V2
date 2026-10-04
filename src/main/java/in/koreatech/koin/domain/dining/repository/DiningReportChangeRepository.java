package in.koreatech.koin.domain.dining.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.DiningReportChange;

public interface DiningReportChangeRepository extends Repository<DiningReportChange, Long> {

    DiningReportChange save(DiningReportChange change);

    List<DiningReportChange> findBySequenceGreaterThanAndSequenceLessThanEqualOrderBySequenceAsc(
        Long cursor, Long lastSequence, Pageable pageable);
}
