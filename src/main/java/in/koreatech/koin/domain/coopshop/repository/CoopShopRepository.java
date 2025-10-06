package in.koreatech.koin.domain.coopshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface CoopShopRepository extends Repository<CoopShop, Integer> {

    CoopShop save(CoopShop coopShop);

    List<CoopShop> findAll();

    Optional<CoopShop> findById(Integer id);

    Optional<CoopShop> findByCoopNameIdAndCoopSemester(Integer coopNameId, CoopSemester semester);

    default CoopShop getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coopShopId : " + id));
    }

    default CoopShop getByCoopNameIdAndCoopSemester(Integer coopNameId, CoopSemester semester) {
        return findByCoopNameIdAndCoopSemester(coopNameId, semester)
            .orElseThrow(() -> CoopShopNotFoundException
                .withDetail(String.format("coopNameId : %d, semesterId: %d", coopNameId, semester.getId())));
    }
}
