package in.koreatech.koin.domain.student.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.student.dto.StudentAcademicInfoUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentAcademicInfoUpdateResponse;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.student.dto.StudentRegisterV2Request;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.student.dto.StudentWithAcademicResponse;
import in.koreatech.koin.domain.student.service.StudentService;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeSubmitRequest;
import in.koreatech.koin.web.host.ServerURL;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudentController implements StudentApi {

    private final StudentService studentService;

    @GetMapping("/user/student/me")
    public ResponseEntity<StudentResponse> getStudent(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        StudentResponse studentResponse = studentService.getStudent(userId);
        return ResponseEntity.ok().body(studentResponse);
    }

    @GetMapping("/user/student/me/academic-info")
    public ResponseEntity<StudentWithAcademicResponse> getStudentWithAcademicInfo(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        StudentWithAcademicResponse response = studentService.getStudentWithAcademicInfo(userId);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/user/student/me")
    public ResponseEntity<StudentUpdateResponse> updateStudent(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @Valid @RequestBody StudentUpdateRequest request
    ) {
        StudentUpdateResponse studentUpdateResponse = studentService.updateStudent(userId, request);
        return ResponseEntity.ok(studentUpdateResponse);
    }

    @PutMapping("/user/student/academic-info")
    public ResponseEntity<StudentAcademicInfoUpdateResponse> updateStudentAcademicInfo(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @Valid @RequestBody StudentAcademicInfoUpdateRequest request
    ) {
        StudentAcademicInfoUpdateResponse response = studentService.updateStudentAcademicInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student/login")
    public ResponseEntity<StudentLoginResponse> studentLogin(
        @RequestBody @Valid StudentLoginRequest request
    ) {
        StudentLoginResponse response = studentService.studentLogin(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("/user/student/register")
    public ResponseEntity<Void> studentRegister(
        @Valid @RequestBody StudentRegisterRequest request,
        @ServerURL String serverURL
    ) {
        studentService.studentRegister(request, serverURL);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/user/authenticate")
    public ModelAndView authenticate(
        @ModelAttribute("auth_token")
        @Valid AuthTokenRequest request
    ) {
        return studentService.authenticate(request);
    }

    @PostMapping("/v2/user/student/register")
    public ResponseEntity<Void> studentRegisterV2(
        @Valid StudentRegisterV2Request request
    ) {
        studentService.studentRegisterV2(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/user/find/password")
    public ResponseEntity<Void> findPassword(
        @RequestBody @Valid FindPasswordRequest request,
        @ServerURL String serverURL
    ) {
        studentService.findPassword(request, serverURL);
        return new ResponseEntity<>(HttpStatusCode.valueOf(201));
    }

    @PutMapping("/user/change/password")
    public ResponseEntity<Void> changePassword(
        @RequestBody UserPasswordChangeRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        studentService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/change/password/config")
    public ModelAndView checkResetToken(
        @ServerURL String serverUrl,
        @RequestParam("reset_token") String resetToken
    ) {
        return studentService.checkResetToken(resetToken, serverUrl);
    }

    @Hidden
    @PostMapping("/user/change/password/submit")
    public ResponseEntity<Void> changePasswordSubmit(
        @RequestBody UserPasswordChangeSubmitRequest request,
        @RequestParam("reset_token") String resetToken
    ) {
        studentService.changePasswordSubmit(request, resetToken);
        return ResponseEntity.ok().build();
    }
}
