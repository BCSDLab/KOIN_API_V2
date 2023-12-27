package in.koreatech.koin.domain.dept.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.domain.Dept;

public interface DeptRepository extends Repository<Dept, String> {
    Optional<Dept> findByDeptNumNumber(Long number);
}
