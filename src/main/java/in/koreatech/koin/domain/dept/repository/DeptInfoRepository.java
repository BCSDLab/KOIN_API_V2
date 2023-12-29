package in.koreatech.koin.domain.dept.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.domain.DeptInfo;

public interface DeptInfoRepository extends Repository<DeptInfo, String> {

    DeptInfo save(DeptInfo deptInfo);

    List<DeptInfo> findAll();
}
