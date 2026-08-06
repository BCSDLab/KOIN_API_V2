package in.koreatech.koin.admin.lecture.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.LECTURES;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.DUPLICATE_LECTURE;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_SEMESTER_FORMAT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_SEMESTER;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Lecture: 강의", description = "관리자 권한으로 강의를 관리한다")
public interface AdminLectureApi {

    @ApiResponseCodes({
        OK,
        INVALID_REQUEST_BODY,
        UNAUTHORIZED_USER,
        FORBIDDEN_ADMIN,
        INVALID_SEMESTER_FORMAT,
        NOT_FOUND_SEMESTER,
        DUPLICATE_LECTURE
    })
    @Operation(summary = "강의 일괄 생성")
    @SecurityRequirement(name = "Jwt Authentication")
    @PostMapping("/admin/lectures")
    @AdminActivityLogging(domain = LECTURES)
    ResponseEntity<Void> createLectures(
        @RequestBody @Valid AdminLectureCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
