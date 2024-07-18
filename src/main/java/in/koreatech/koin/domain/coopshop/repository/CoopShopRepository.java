package in.koreatech.koin.domain.coopshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopShop;

public interface CoopShopRepository extends Repository<CoopShop, Integer> {

    CoopShop save(CoopShop coopShop);

    List<CoopShop> findAll();

    List<CoopShop> findAllByIsDeletedFalse();

    Optional<CoopShop> findById(Integer id);

    Optional<CoopShop> findByName(String name);

    default CoopShop getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coopShopId : " + id));
    }

    default CoopShop getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coopShopType : " + name));
    }
}
