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
import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequest;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequestV2;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentWithAcademicResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentRequestV2;
import in.koreatech.koin.domain.student.dto.UpdateStudentResponse;
import in.koreatech.koin.domain.student.service.StudentService;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.ChangeUserPasswordRequest;
import in.koreatech.koin.domain.user.dto.ChangeUserPasswordSubmitRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
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
    public ResponseEntity<UpdateStudentResponse> updateStudent(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @Valid @RequestBody UpdateStudentRequest request
    ) {
        UpdateStudentResponse updateStudentResponse = studentService.updateStudent(userId, request);
        return ResponseEntity.ok(updateStudentResponse);
    }

    @PutMapping("/v2/users/students/me")
    public ResponseEntity<UpdateStudentResponse> updateStudentV2(
        @Valid @RequestBody UpdateStudentRequestV2 request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        UpdateStudentResponse updateStudentResponse = studentService.updateStudentV2(userId, request);
        return ResponseEntity.ok(updateStudentResponse);
    }

    @PutMapping("/user/student/academic-info")
    public ResponseEntity<UpdateStudentAcademicInfoResponse> updateStudentAcademicInfo(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @Valid @RequestBody UpdateStudentAcademicInfoRequest request
    ) {
        UpdateStudentAcademicInfoResponse response = studentService.updateStudentAcademicInfo(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student/login")
    public ResponseEntity<StudentLoginResponse> studentLogin(
        @RequestBody @Valid StudentLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        StudentLoginResponse response = studentService.studentLogin(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("/user/student/register")
    public ResponseEntity<Void> studentRegister(
        @Valid @RequestBody RegisterStudentRequest request,
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

    @PostMapping("/v2/users/students/register")
    public ResponseEntity<Void> studentRegisterV2(
        @RequestBody @Valid RegisterStudentRequestV2 request
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
        @RequestBody ChangeUserPasswordRequest request,
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
        @RequestBody ChangeUserPasswordSubmitRequest request,
        @RequestParam("reset_token") String resetToken
    ) {
        studentService.changePasswordSubmit(request, resetToken);
        return ResponseEntity.ok().build();
    }
}
