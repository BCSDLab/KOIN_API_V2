package in.koreatech.koin.domain.student.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.model.Major;

public interface MajorRepository extends Repository<Major, Integer> {

    Major save(Major major);

    void saveAll(Iterable<Major> majors);

    Optional<Major> findByName(String name);

    Optional<List<Major>> findAllByDepartmentId(Integer departmentId);

    default Major getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }

    default List<Major> getAllByDepartmentId(Integer departmentId) {
        return findAllByDepartmentId(departmentId)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("departmentId: " + departmentId));
    }
}
