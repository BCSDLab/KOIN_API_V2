package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.graduation.model.Department;

public interface DepartmentRepository extends Repository<Department, Integer> {
    Optional<Department> findByName(String name);

    default Department getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }
}
