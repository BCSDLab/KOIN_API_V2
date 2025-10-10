package in.koreatech.koin.admin.history.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.history.dto.AdminHistoriesCondition;
import in.koreatech.koin.admin.history.exception.AdminActivityHistoryNotFoundException;
import in.koreatech.koin.admin.history.model.AdminActivityHistory;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminActivityHistoryRepository extends Repository<AdminActivityHistory, Integer> {

    AdminActivityHistory save(AdminActivityHistory adminActivityHistory);

    Optional<AdminActivityHistory> findById(Integer id);

    default AdminActivityHistory getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> new AdminActivityHistoryNotFoundException("admin_activity_history id: " + id));
    }

    @Query("SELECT COUNT(*) FROM AdminActivityHistory")
    Integer countAdminActivityHistory();

    @Query("""
        SELECT a FROM AdminActivityHistory a WHERE
        (:#{#condition.requestMethod?.name()} IS NULL OR a.requestMethod = :#{#condition.requestMethod}) AND
        (:#{#condition.domainName?.name()} IS NULL OR a.domainName = :#{#condition.domainName}) AND
        (:#{#condition.domainId} IS NULL OR a.domainId = :#{#condition.domainId})
        """)
    Page<AdminActivityHistory> findByConditions(@Param("condition") AdminHistoriesCondition adminsCondition,
        Pageable pageable);
}
