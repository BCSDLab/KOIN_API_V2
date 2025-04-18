package in.koreatech.koin.domain.dept.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dept.dto.DepartmentAndMajorResponse;
import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.service.DeptService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DeptController implements DeptApi {

    private final DeptService deptService;

    @GetMapping("/dept")
    public ResponseEntity<DeptResponse> getDept(
        @RequestParam(value = "dept_num") String deptNumber
    ) {
        DeptResponse foundDepartment = deptService.getById(deptNumber);
        if (foundDepartment == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(foundDepartment);
    }

    @GetMapping("/depts")
    public ResponseEntity<List<DeptsResponse>> getAllDept() {
        List<DeptsResponse> response = deptService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/depts/major")
    public ResponseEntity<List<DepartmentAndMajorResponse>> getAllDeptAndMajor() {
        List<DepartmentAndMajorResponse> response = deptService.getAllDepartmentsWithMajors();
        return ResponseEntity.ok(response);
    }

}
