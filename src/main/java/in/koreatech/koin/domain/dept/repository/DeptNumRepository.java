package in.koreatech.koin.domain.dept.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.model.DeptNum;
import in.koreatech.koin.domain.dept.model.DeptNumId;

public interface DeptNumRepository extends Repository<DeptNum, DeptNumId> {

    DeptNum save(DeptNum deptNum);

    Optional<DeptNum> findByNumber(Long number);
}
