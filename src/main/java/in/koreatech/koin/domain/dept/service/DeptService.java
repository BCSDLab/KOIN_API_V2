package in.koreatech.koin.domain.dept.service;

import static in.koreatech.koin.domain.dept.model.Dept.NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.dto.DepartmentAndMajorResponse;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;

    public DeptResponse getById(String id) {
        Optional<Dept> dept = Dept.findByNumber(id);
        return dept.map(value -> DeptResponse.from(id, value)).orElse(null);
    }

    public List<DeptsResponse> getAll() {
        return Dept.findAll()
            .stream()
            .filter(dept -> !NEW_ENERGY_MATERIALS_CHEMICAL_ENGINEERING.equals(dept))
            .map(DeptsResponse::from)
            .toList();
    }

    public List<DepartmentAndMajorResponse> getAllDepartmentsWithMajors() {
        List<Department> departments = departmentRepository.findAll().orElse(Collections.emptyList());
        return departments.stream().map(department -> {
            List<Major> majors = majorRepository.findAllByDepartmentId(department.getId())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(major -> major.getName() != null)
                    .toList();
            return DepartmentAndMajorResponse.of(department, majors);
        }).toList();
    }
}
