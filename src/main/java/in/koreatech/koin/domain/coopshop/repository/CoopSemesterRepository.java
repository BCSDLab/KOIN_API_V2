package in.koreatech.koin.domain.coopshop.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.coopshop.exception.CoopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;

public interface CoopSemesterRepository extends Repository<CoopSemester, Integer> {

    CoopSemester save(CoopSemester coopSemester);

    Optional<CoopSemester> findByIsApplied(boolean isApplied);

    default CoopSemester getByIsApplied(boolean isApplied) {
        return findByIsApplied(isApplied)
            .orElseThrow(() -> CoopSemesterNotFoundException.withDetail(""));
    }

    @Query("""
        SELECT coopSemester FROM CoopSemester coopSemester
        WHERE coopSemester.fromDate <= :date AND coopSemester.toDate >= :date
        ORDER BY coopSemester.fromDate DESC, coopSemester.id DESC
        """)
    List<CoopSemester> findAllValidOn(@Param("date") LocalDate date);
}
