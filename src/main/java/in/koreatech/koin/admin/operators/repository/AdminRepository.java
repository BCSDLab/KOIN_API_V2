package in.koreatech.koin.admin.operators.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.operators.dto.request.AdminsCondition;
import in.koreatech.koin.admin.operators.exception.AdminNotFoundException;
import in.koreatech.koin.admin.operators.model.Admin;

public interface AdminRepository extends Repository<Admin, Integer> {

    Admin save(Admin admin);

    Optional<Admin> findById(Integer id);

    Optional<Admin> findByEmail(String email);

    default Admin getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> AdminNotFoundException.withDetail("adminId : " + id));
    }

    default Admin getByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> AdminNotFoundException.withDetail("email : " + email));
    }

    @Query("SELECT COUNT(*) FROM Admin")
    Integer countAdmins();

    @Query("""
        SELECT a FROM Admin a WHERE
        (:#{#condition.isAuthed} IS NULL OR a.user.isAuthed = :#{#condition.isAuthed}) AND
        (:#{#condition.trackType?.name()} IS NULL OR a.trackType = :#{#condition.trackType}) AND
        (:#{#condition.teamType?.name()} IS NULL OR a.teamType = :#{#condition.teamType})
        """)
    Page<Admin> findByConditions(@Param("condition") AdminsCondition adminsCondition, Pageable pageable);

}
