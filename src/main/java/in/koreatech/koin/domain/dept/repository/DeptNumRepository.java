package in.koreatech.koin.domain.dept.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dept.domain.DeptNum;
import in.koreatech.koin.domain.dept.domain.DeptType;

public interface DeptNumRepository extends Repository<DeptNum, String> {

    DeptNum getByNumber(Long number);

    Long findNumberByName(DeptType name);
}
