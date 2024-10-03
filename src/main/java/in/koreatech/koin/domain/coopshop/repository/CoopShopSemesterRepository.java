package in.koreatech.koin.domain.coopshop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;

public interface CoopShopSemesterRepository extends Repository<CoopShopSemester, Integer> {

    Optional<CoopShopSemester> findByIsApplied(boolean isApplied);

    default CoopShopSemester getByIsApplied(boolean isApplied) {
        return findByIsApplied(isApplied)
            .orElseThrow(() -> CoopShopSemesterNotFoundException.withDetail(""));
    }
}
