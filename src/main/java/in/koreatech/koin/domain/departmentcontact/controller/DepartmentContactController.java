package in.koreatech.koin.domain.departmentcontact.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.departmentcontact.dto.DepartmentContactResponse;
import in.koreatech.koin.domain.departmentcontact.service.DepartmentContactService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/department-contacts")
public class DepartmentContactController implements DepartmentContactApi {

    private final DepartmentContactService departmentContactService;

    @GetMapping
    public ResponseEntity<List<DepartmentContactResponse>> getDepartmentContacts(
        @RequestParam(name = "department", required = false) String department
    ) {
        return ResponseEntity.ok(departmentContactService.getDepartmentContacts(department));
    }
}
