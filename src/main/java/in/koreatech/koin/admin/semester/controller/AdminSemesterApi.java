package in.koreatech.koin.admin.semester.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.semester.dto.AdminSemesterCreateRequest;
import in.koreatech.koin.admin.semester.dto.AdminSemesterResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(ADMIN) Semester : 학기", description = "관리자 권한으로 학기 정보를 관리한다")
public interface AdminSemesterApi {

    @ApiResponseCodes({
        OK,
        INVALID_SEMESTER_FORMAT,
        DUPLICATE_SEMESTER,
        INVALID_START_DATE_AFTER_END_DATE,
        OVERLAPPING_SEMESTER_DATE_RANGE
    })
    @Operation(summary = "(ADMIN) 생협 학기 생성", description = """
        - semester의 경우 {2자리 연도}-{학기명}으로 보내야합니다.
        """)
    @PostMapping("/admin/coopshop/semesters")
    ResponseEntity<Void> createCoopshopSemester(
        @Valid @RequestBody AdminSemesterCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @Operation(summary = "(ADMIN) 생협 학기 리스트 조회")
    @GetMapping("/admin/coopshop/semesters")
    ResponseEntity<List<AdminSemesterResponse>> getCoopshopSemesters(
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
