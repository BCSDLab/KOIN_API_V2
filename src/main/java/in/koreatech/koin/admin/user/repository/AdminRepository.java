package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.user.exception.AdminNotFoundException;
import in.koreatech.koin.admin.user.model.Admin;

public interface AdminRepository extends Repository<Admin, Integer> {
    Admin save(Admin admin);

    Optional<Admin> findById(Integer id);

    default Admin getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> AdminNotFoundException.withDetail("adminId : " + id));
    }
}
