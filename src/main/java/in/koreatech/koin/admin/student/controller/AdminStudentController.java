package in.koreatech.koin.admin.student.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.student.service.AdminStudentService;
import in.koreatech.koin.admin.student.dto.AdminStudentResponse;
import in.koreatech.koin.admin.student.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.student.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.student.dto.AdminStudentsResponse;
import in.koreatech.koin.admin.student.dto.StudentsCondition;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminStudentController implements AdminStudentApi {

    private final AdminStudentService adminStudentService;

    @GetMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentResponse> getStudent(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentResponse response = adminStudentService.getStudent(id);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/admin/students")
    public ResponseEntity<AdminStudentsResponse> getStudents(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Boolean isAuthed,
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) String email,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        StudentsCondition studentsCondition = new StudentsCondition(page, limit, isAuthed, nickname, email);
        AdminStudentsResponse response = adminStudentService.getStudents(studentsCondition);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentUpdateResponse> updateStudent(
        @Valid @RequestBody AdminStudentUpdateRequest adminRequest,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentUpdateResponse response = adminStudentService.updateStudent(id, adminRequest);
        return ResponseEntity.ok().body(response);
    }
}
