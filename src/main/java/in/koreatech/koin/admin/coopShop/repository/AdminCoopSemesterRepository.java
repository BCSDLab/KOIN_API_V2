package in.koreatech.koin.admin.coopShop.repository;

import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_COOP_SEMESTER;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.model.CoopSemester;
import in.koreatech.koin.global.exception.CustomException;

public interface AdminCoopSemesterRepository extends Repository<CoopSemester, Integer> {

    Page<CoopSemester> findAll(Pageable pageable);

    Integer count();

    List<CoopSemester> findAll();

    Optional<CoopSemester> findById(Integer id);

    default CoopSemester getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CustomException.of(NOT_FOUND_COOP_SEMESTER, "semester_id : " + id));
    }
}
