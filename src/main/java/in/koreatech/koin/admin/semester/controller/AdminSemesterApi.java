package in.koreatech.koin.admin.semester.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.COOP_SEMESTER;
import static in.koreatech.koin.admin.history.enums.DomainType.SEMESTER;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.semester.dto.AdminSemesterCreateRequest;
import in.koreatech.koin.admin.semester.dto.AdminSemesterResponse;
import in.koreatech.koin.admin.semester.dto.AdminTimetableSemesterCreateRequest;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(ADMIN) Semester : 학기", description = "관리자 권한으로 학기 정보를 관리한다")
public interface AdminSemesterApi {

    @ApiResponseCodes({
        OK,
        INVALID_REQUEST_BODY,
        UNAUTHORIZED_USER,
        FORBIDDEN_ADMIN,
        NOT_READABLE_HTTP_MESSAGE,
        DUPLICATE_SEMESTER
    })
    @Operation(summary = "(ADMIN) 시간표 학기 생성", description = """
        - `year`와 `term`을 사용해 시간표 학기를 생성합니다.
        - `term`은 `FIRST`, `SECOND`, `SUMMER`, `WINTER` 중 하나입니다.
        - 이미 같은 연도와 학기가 존재하면 생성하지 않습니다.
        """)
    @SecurityRequirement(name = "Jwt Authentication")
    @AdminActivityLogging(domain = SEMESTER)
    @PostMapping("/admin/semesters")
    ResponseEntity<Void> createSemester(
        @Valid @RequestBody AdminTimetableSemesterCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

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
    @AdminActivityLogging(domain = COOP_SEMESTER)
    @PostMapping("/admin/coopshop/semesters")
    ResponseEntity<Void> createCoopshopSemester(
        @Valid @RequestBody AdminSemesterCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponseCodes({
        OK
    })
    @Operation(summary = "(ADMIN) 생협 학기 리스트 조회", description = """
        - 응답값은 최신 학기 -> 과거 학기 순으로 정렬되서 나갑니다.
        """)
    @GetMapping("/admin/coopshop/semesters")
    ResponseEntity<List<AdminSemesterResponse>> getCoopshopSemesters(
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
