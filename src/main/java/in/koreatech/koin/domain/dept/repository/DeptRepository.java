package in.koreatech.koin.domain.dept.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.domain.Dept;

public interface DeptRepository extends Repository<Dept, Long>  {
    Dept findDeptBy(Long id);
}
