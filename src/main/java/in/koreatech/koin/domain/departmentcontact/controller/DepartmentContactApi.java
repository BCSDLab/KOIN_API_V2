package in.koreatech.koin.domain.departmentcontact.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) DepartmentContact: 부서 업무 연락처", description = "학교 부서의 업무별 전화번호를 조회한다")
@RequestMapping("/department-contacts")
public interface DepartmentContactApi {

    @Operation(summary = "부서 업무 연락처 조회", description = "부서명을 전달하지 않으면 전체 연락처를 반환합니다.")
    @GetMapping
    ResponseEntity<List<DepartmentContactResponse>> getDepartmentContacts(
        @RequestParam(name = "department", required = false) String department
    );
}
