package in.koreatech.koin.domain.student.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface DepartmentRepository extends Repository<Department, Integer> {

    Department save(Department department);

    void saveAll(Iterable<Department> departments);

    Optional<Department> findByName(String name);

    Optional<List<Department>> findAll();

    default Department getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }
}
