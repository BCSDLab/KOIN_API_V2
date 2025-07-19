package in.koreatech.koin.domain.student.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
import in.koreatech.koin.domain.user.dto.UserChangePasswordRequest;
import in.koreatech.koin.domain.user.dto.UserFindPasswordRequest;
import in.koreatech.koin.web.host.ServerURL;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Normal) Student: 회원", description = "학생 관련 API")
public interface StudentApi {

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "학생 정보 조회")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/user/student/me")
    ResponseEntity<StudentResponse> getStudent(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "학생 정보 조회(학적 포함)")
    @SecurityRequirement(name = "Jwt Authentication")
    @GetMapping("/user/student/me/academic-info")
    ResponseEntity<StudentWithAcademicResponse> getStudentWithAcademicInfo(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학생 정보 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/user/student/me")
    ResponseEntity<UpdateStudentResponse> updateStudent(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @Valid UpdateStudentRequest updateStudentRequest
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학생 정보 수정 V2")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/v2/users/students/me")
    ResponseEntity<UpdateStudentResponse> updateStudentV2(
        @Valid @RequestBody UpdateStudentRequestV2 request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학생 학적 정보 수정")
    @SecurityRequirement(name = "Jwt Authentication")
    @PutMapping("/user/student/academic-info")
    ResponseEntity<UpdateStudentAcademicInfoResponse> updateStudentAcademicInfo(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId,
        @Valid @RequestBody UpdateStudentAcademicInfoRequest request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "학생 로그인 (Deprecated)")
    @PostMapping("/student/login")
    ResponseEntity<StudentLoginResponse> studentLogin(
        @RequestBody @Valid StudentLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "회원가입")
    @PostMapping("/user/student/register")
    ResponseEntity<Void> studentRegister(
        @RequestBody @Valid RegisterStudentRequest registerStudentRequest,
        @ServerURL String serverURL
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "학생 회원가입(문자 인증)")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/v2/users/students/register")
    ResponseEntity<Void> studentRegisterV2(
        @RequestBody @Valid RegisterStudentRequestV2 request
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true)))
        }
    )
    @Operation(summary = "비밀번호 초기(변경) 메일 발송")
    @PostMapping("/user/find/password")
    ResponseEntity<Void> findPassword(
        @RequestBody @Valid UserFindPasswordRequest userFindPasswordRequest,
        @ServerURL String serverURL
    );

    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
        }
    )
    @Operation(summary = "비밀번호 변경")
    @PutMapping("/user/change/password")
    ResponseEntity<Void> changePassword(
        @RequestBody UserChangePasswordRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    );
}
