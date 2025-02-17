package in.koreatech.koin.admin.student.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.model.Department;

public interface AdminDepartmentRepository extends Repository<Department, Integer> {

    Optional<Department> findByName(String name);

    default Department getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }
}
