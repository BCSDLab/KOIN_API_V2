package in.koreatech.koin.domain.dept.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dept.dto.DeptResponse;
import in.koreatech.koin.domain.dept.dto.DeptsResponse;
import in.koreatech.koin.domain.dept.service.DeptService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/dept")
    public ResponseEntity<DeptResponse> findDept(@RequestParam(value = "dept_num") Long deptNumber) {
        DeptResponse foundDepartment = deptService.findDeptBy(deptNumber);
        return ResponseEntity.ok(foundDepartment);
    }

    @GetMapping("/depts")
    public ResponseEntity<List<DeptsResponse>> findAllDept() {
        List<DeptsResponse> response = deptService.findAllDept();
        return ResponseEntity.ok(response);
    }
}
