package in.koreatech.koin.domain.user.controller;

import in.koreatech.koin.domain.auth.StudentAuth;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/user/student/me")
    public ResponseEntity<StudentResponse> getStudent(@StudentAuth Student student) {
        StudentResponse studentResponse = studentService.getStudent(student);
        return ResponseEntity.ok().body(studentResponse);
    }
}
