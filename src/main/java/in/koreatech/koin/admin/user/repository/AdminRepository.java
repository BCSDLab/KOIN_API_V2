package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;

public interface AdminRepository extends Repository<Admin, Integer> {
    Admin save(Admin admin);

    Optional<Admin> findById(Integer id);

    default Admin getById(Integer adminId) {
        return findById(adminId)
            .orElseThrow(() -> UserNotFoundException.withDetail("adminId: " + adminId));
    }
}
