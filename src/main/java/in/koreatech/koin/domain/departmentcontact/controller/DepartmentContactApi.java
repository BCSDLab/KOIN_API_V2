package in.koreatech.koin.domain.departmentcontact.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentCategoryContactsResponse;
import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactsResponse;
import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) DepartmentContact: 부서 업무 연락처", description = "학교 부서의 업무별 전화번호를 조회한다")
@RequestMapping("/department-contacts")
public interface DepartmentContactApi {

    @Operation(summary = "전체 부서 업무 연락처 조회", description = "키워드로 카테고리명, 부서명, 업무를 검색할 수 있습니다.")
    @GetMapping
    ResponseEntity<DepartmentContactsResponse> getDepartmentContacts(
        @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    );

    @Operation(
        summary = "카테고리별 부서 업무 연락처 조회",
        description = "선택한 카테고리 안에서 카테고리명, 부서명, 업무를 검색합니다."
    )
    @GetMapping("/{category}")
    ResponseEntity<DepartmentCategoryContactsResponse> getDepartmentContactsByCategory(
        @PathVariable DepartmentCategory category,
        @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    );
}
