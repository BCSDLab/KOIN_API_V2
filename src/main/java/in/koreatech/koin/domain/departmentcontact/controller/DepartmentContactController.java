package in.koreatech.koin.domain.departmentcontact.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentCategoryContactsResponse;
import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactsResponse;
import in.koreatech.koin.domain.departmentcontact.enums.DepartmentCategory;
import in.koreatech.koin.domain.departmentcontact.service.DepartmentContactService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/department-contacts")
public class DepartmentContactController implements DepartmentContactApi {

    private final DepartmentContactService departmentContactService;

    @GetMapping
    public ResponseEntity<DepartmentContactsResponse> getDepartmentContacts(
        @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(departmentContactService.getDepartmentContacts(keyword));
    }

    @GetMapping("/{category}")
    public ResponseEntity<DepartmentCategoryContactsResponse> getDepartmentContactsByCategory(
        @PathVariable DepartmentCategory category,
        @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return ResponseEntity.ok(departmentContactService.getDepartmentContactsByCategory(category, keyword));
    }
}
