package in.koreatech.koin.domain.dept.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.dept.dto.DepartmentAndMajorResponse;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.repository.AcademicProgramYearRepository;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeptService {

    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final AcademicProgramYearRepository academicProgramYearRepository;

    public DeptResponse getById(String id) {
        Optional<Dept> dept = Dept.findByNumber(id);
        return dept.map(value -> DeptResponse.from(id, value)).orElse(null);
    }

    public List<DeptsResponse> getAll() {
        return departmentRepository.findAllByIsSelectableTrueOrderByDisplayOrderAscIdAsc()
            .stream()
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

    public List<DepartmentAndMajorResponse> getAllDepartmentsWithMajors(String studentYear) {
        if (studentYear == null) {
            return getAllDepartmentsWithMajors();
        }
        if (!studentYear.matches("[0-9]{4}")) {
            throw new IllegalArgumentException("학번 연도는 네 자리 숫자여야 합니다.");
        }
        List<AcademicProgramYear> programs = academicProgramYearRepository
            .findAllByStudentYearOrderByDepartmentIdAscIdAsc(studentYear);
        if (programs.isEmpty()) {
            return getAllDepartmentsWithMajors();
        }

        Map<Integer, Department> departments = new LinkedHashMap<>();
        Map<Integer, List<Major>> majors = new LinkedHashMap<>();
        for (AcademicProgramYear program : programs) {
            Integer departmentId = program.getDepartment().getId();
            departments.put(departmentId, program.getDepartment());
            majors.computeIfAbsent(departmentId, ignored -> new ArrayList<>());
            if (program.getMajor() != null && program.getMajor().getName() != null) {
                majors.get(departmentId).add(program.getMajor());
            }
        }
        return departments.entrySet().stream()
            .map(entry -> DepartmentAndMajorResponse.of(entry.getValue(), majors.get(entry.getKey())))
            .toList();
    }
}
