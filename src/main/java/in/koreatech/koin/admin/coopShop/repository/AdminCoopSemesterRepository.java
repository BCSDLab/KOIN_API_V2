package in.koreatech.koin.admin.coopShop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopSemesterNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface AdminCoopSemesterRepository extends Repository<CoopSemester, Integer> {

    Page<CoopSemester> findAll(Pageable pageable);

    Integer count();

    List<CoopSemester> findAll();

    Optional<CoopSemester> findById(Integer id);

    default CoopSemester getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CoopSemesterNotFoundException.withDetail("semester_id : " + id));
    }
}
