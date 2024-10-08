package in.koreatech.koin.domain.coopshop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.CoopShopType;

public interface CoopShopRepository extends Repository<CoopShop, Integer> {

    CoopShop save(CoopShop coopShop);

    List<CoopShop> findAll();

    Optional<CoopShop> findById(Integer id);

    Optional<CoopShop> findByNameAndCoopSemesterId(String name, Integer semesterId);

    default CoopShop getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coopShopId : " + id));
    }

    default CoopShop getByNameAndCoopSemesterId(CoopShopType name, Integer semesterId) {
        return findByNameAndCoopSemesterId(name.getCoopShopName(), semesterId)
            .orElseThrow(() -> CoopShopNotFoundException
                .withDetail("coopShopName : " + name.getCoopShopName() + ", semesterId: " + semesterId));
    }
}
