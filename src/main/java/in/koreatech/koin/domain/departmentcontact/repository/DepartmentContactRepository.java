package in.koreatech.koin.domain.departmentcontact.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.departmentcontact.model.DepartmentContact;

public interface DepartmentContactRepository extends Repository<DepartmentContact, Integer> {

    List<DepartmentContact> findAllByOrderByIdAsc();

    List<DepartmentContact> findAllByDepartmentOrderByIdAsc(String department);
}
