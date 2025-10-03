package in.koreatech.koin.domain.coopshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopName;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface CoopNameRepository extends Repository<CoopName, Integer> {

    CoopName save(CoopName coopNames);

    List<CoopName> findAll();

    Optional<CoopName> findById(Integer id);

    Optional<CoopName> findByName(String coopName);

    default CoopName getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coopShopId : " + id));
    }

    default CoopName getByName(CoopShopType coopShopType) {
        return findByName(coopShopType.getCoopShopName())
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coopShopType : " + coopShopType));
    }
}
