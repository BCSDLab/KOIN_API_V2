package in.koreatech.koin.domain.dept.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.model.DeptInfo;
import in.koreatech.koin.domain.dept.model.DeptNumId;

public interface DeptInfoRepository extends Repository<DeptInfo, DeptNumId> {

    DeptInfo save(DeptInfo deptInfo);

    List<DeptInfo> findAll();
}
