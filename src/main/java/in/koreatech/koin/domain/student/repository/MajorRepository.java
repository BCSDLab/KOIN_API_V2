package in.koreatech.koin.domain.student.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.student.exception.DepartmentNotFoundException;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface MajorRepository extends Repository<Major, Integer> {

    Major save(Major major);

    void saveAll(Iterable<Major> majors);

    Optional<Major> findByName(String name);

    Optional<List<Major>> findAllByDepartmentId(Integer departmentId);

    List<Major> findByDepartmentId(Integer departmentId);

    boolean existsByName(String name);

    Optional<Major> findByNameAndDepartmentId(String name, Integer departmentId);

    Optional<Major> findFirstByDepartmentIdOrderByIdAsc(Integer departmentId);

    default Major getByName(String name) {
        return findByName(name)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name));
    }

    default Major getByNameAndDepartmentId(String name, Integer departmentId) {
        return findByNameAndDepartmentId(name, departmentId)
            .orElseThrow(() -> DepartmentNotFoundException.withDetail("name: " + name + ", departmentId: " + departmentId));
    }
}
