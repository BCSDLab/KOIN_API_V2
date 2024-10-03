package in.koreatech.koin.admin.coopShop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopShopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopShopSemester;

public interface AdminCoopShopSemesterRepository extends Repository<CoopShopSemester, Integer> {

    Page<CoopShopSemester> findAll(Pageable pageable);

    Integer count();

    List<CoopShopSemester> findAll();

    Optional<CoopShopSemester> findById(Integer id);

    default CoopShopSemester getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopShopSemesterNotFoundException.withDetail("semester_id : " + id));
    }
}
