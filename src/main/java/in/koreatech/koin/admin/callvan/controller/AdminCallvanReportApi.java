package in.koreatech.koin.admin.callvan.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.callvan.dto.AdminCallvanReportProcessRequest;
import in.koreatech.koin.admin.callvan.dto.AdminCallvanReportsResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Admin) Callvan: Reports", description = "Admin callvan report management")
@RequestMapping("/admin/callvan/reports")
public interface AdminCallvanReportApi {

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
        FORBIDDEN_ADMIN
    })
    @Operation(summary = "콜벤 신고 목록 조회", description = """
        콜벤 사용자 신고 접수 목록을 조회합니다.
        - `only_pending=true` 이면 미처리 신고(PENDING)만 조회합니다.
        - 각 항목에는 피신고자 정보, 신고 사유, 첨부 이미지, 처리 유형, 누적 신고 이력이 포함됩니다.
        """)
    @GetMapping
    ResponseEntity<AdminCallvanReportsResponse> getCallvanReports(
        @RequestParam(name = "only_pending", required = false, defaultValue = "false") Boolean onlyPending,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    );

    @ApiResponseCodes({
        OK,
        UNAUTHORIZED_USER,
        FORBIDDEN_ADMIN,
        INVALID_REQUEST_BODY,
        NOT_FOUND_CALLVAN_REPORT,
        CALLVAN_REPORT_ALREADY_PROCESSED
    })
    @Operation(summary = "콜벤 신고 처리", description = """
        콜벤 신고를 처리합니다.
        - `WARNING`: 신고 확정 후 주의 안내 알림을 발송합니다.
        - `TEMPORARY_RESTRICTION_14_DAYS`: 신고 확정 후 14일간 새 모집/참여를 제한합니다.
        - `PERMANENT_RESTRICTION`: 신고 확정 후 콜벤 기능을 영구 제한합니다.
        - `REJECT`: 신고를 반려하고 상태를 REJECTED로 변경합니다.
        """)
    @PostMapping("/{reportId}/process")
    ResponseEntity<Void> processCallvanReport(
        @Parameter(in = PATH) @PathVariable Integer reportId,
        @RequestBody @Valid AdminCallvanReportProcessRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    );
}
