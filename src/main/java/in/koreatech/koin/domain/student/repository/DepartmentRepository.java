package in.koreatech.koin.domain.student.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.model.Department;

public interface DepartmentRepository extends Repository<Department, Integer> {

    Department save(Department department);

    Optional<Department> findByName(String name);

    default Department getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }
}
