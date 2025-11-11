package in.koreatech.koin.admin.semester.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.coopshop.model.CoopSemester;

public interface AdminCoopShopSemesterRepository extends Repository<CoopSemester, Integer> {

    Optional<CoopSemester> findBySemester(String semester);

    void save(CoopSemester coopSemester);

    @Query("""
        SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END
        FROM CoopSemester cs
        WHERE :fromDate <= cs.toDate AND :toDate >= cs.fromDate
        """)
    boolean existsOverlappingDateRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
