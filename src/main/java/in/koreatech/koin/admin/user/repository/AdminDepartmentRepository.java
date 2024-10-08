package in.koreatech.koin.admin.user.repository;

import in.koreatech.koin.domain.graduation.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.graduation.model.Department;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface AdminDepartmentRepository extends Repository<Department, Integer> {

    Optional<Department> findByName(String name);

    default Department getByName(String name) {
        return findByName(name)
                .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }
}
