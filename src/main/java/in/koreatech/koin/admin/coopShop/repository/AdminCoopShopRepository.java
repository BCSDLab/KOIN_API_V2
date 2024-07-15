package in.koreatech.koin.admin.coopShop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopShop;

public interface AdminCoopShopRepository extends Repository<CoopShop, Integer> {

    Page<CoopShop> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    Integer countAllByIsDeleted(boolean isDeleted);

    List<CoopShop> findAll();

    CoopShop save(CoopShop coopShop);

    Optional<CoopShop> findByName(String name);

    default CoopShop getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coop_name : " + name));
    }

    Optional<CoopShop> findById(Integer id);

    default CoopShop getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopShopNotFoundException.withDetail("coop_id : " + id));
    }
}
