package in.koreatech.koin.domain.coopshop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;

public interface CoopSemesterRepository extends Repository<CoopSemester, Integer> {

    CoopSemester save(CoopSemester coopSemester);

    Optional<CoopSemester> findByIsApplied(boolean isApplied);

    default CoopSemester getByIsApplied(boolean isApplied) {
        return findByIsApplied(isApplied)
            .orElseThrow(() -> CoopSemesterNotFoundException.withDetail(""));
    }

    Optional<CoopSemester> findTopByOrderByToDateDesc();

    default CoopSemester getTopByOrderByToDateDesc() {
        return findTopByOrderByToDateDesc()
            .orElseThrow(() -> CoopSemesterNotFoundException.withDetail(""));
    }
}
