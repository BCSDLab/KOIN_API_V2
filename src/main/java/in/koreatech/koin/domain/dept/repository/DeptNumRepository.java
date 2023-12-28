package in.koreatech.koin.domain.dept.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.domain.DeptNum;

public interface DeptNumRepository extends Repository<DeptNum, String> {

    DeptNum save(DeptNum deptNum);
}
