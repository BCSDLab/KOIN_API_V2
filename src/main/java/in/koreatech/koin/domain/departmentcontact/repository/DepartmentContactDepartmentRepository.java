package in.koreatech.koin.domain.departmentcontact.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import in.koreatech.koin.domain.departmentcontact.model.DepartmentContactDepartment;

public interface DepartmentContactDepartmentRepository extends Repository<DepartmentContactDepartment, Integer> {

    @EntityGraph(attributePaths = "contacts")
    List<DepartmentContactDepartment> findAllByOrderByDisplayOrderAsc();

    @EntityGraph(attributePaths = "contacts")
    List<DepartmentContactDepartment> findAllByCategoryOrderByDisplayOrderAsc(DepartmentCategory category);
}
