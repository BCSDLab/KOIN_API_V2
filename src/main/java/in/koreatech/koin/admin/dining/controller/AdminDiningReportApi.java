package in.koreatech.koin.admin.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_ADMIN;
import static in.koreatech.koin.global.code.ApiResponseCode.FORBIDDEN_USER_TYPE;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.UNAUTHORIZED_USER;
import static in.koreatech.koin.global.code.ApiResponseCode.WITHDRAWN_USER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.dining.dto.AdminDiningReportResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportPageResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportSummaryResponse;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(ADMIN) Dining: 식단 품절 제보 조회")
@SecurityRequirement(name = "Jwt Authentication")
public interface AdminDiningReportApi {

    @ApiResponseCodes({OK, ILLEGAL_ARGUMENT, UNAUTHORIZED_USER, WITHDRAWN_USER, FORBIDDEN_USER_TYPE, FORBIDDEN_ADMIN})
    @Operation(summary = "식단 품절 제보 목록 조회", description = """
        - 인증된 ADMIN이 제보 요약을 조회합니다. 관리자 API는 조회 전용입니다.
        - only_pending=true이면 PENDING 제보만 반환하며, 기본값 false이면 모든 상태를 조회합니다.
        - 접수 시각·제보 ID 내림차순으로 반환하며, 조건에 맞는 제보가 없으면 빈 목록을 반환합니다.
        """)
    @GetMapping("/admin/dining/soldout-reports")
    ResponseEntity<DiningReportPageResponse<DiningReportSummaryResponse>> getReports(
        @Auth(permit = ADMIN) Integer adminId,
        @Parameter(description = "대기 제보만 조회할지 여부", example = "false", schema = @Schema(defaultValue = "false"))
        @RequestParam(name = "only_pending", defaultValue = "false") boolean onlyPending,
        @Parameter(description = "페이지 번호. 1부터 시작하며 전체 페이지 범위로 보정합니다.", example = "1",
            schema = @Schema(defaultValue = "1"))
        @RequestParam(defaultValue = "1") Integer page,
        @Parameter(description = "페이지당 제보 수. 1~50으로 보정합니다.", example = "10",
            schema = @Schema(defaultValue = "10"))
        @RequestParam(defaultValue = "10") Integer limit);

    @ApiResponseCodes({
        OK, ILLEGAL_ARGUMENT, UNAUTHORIZED_USER, WITHDRAWN_USER,
        FORBIDDEN_USER_TYPE, FORBIDDEN_ADMIN, NOT_FOUND_DINING_REPORT
    })
    @Operation(summary = "식단 품절 제보 상세 조회",
        description = "인증된 ADMIN이 사진·처리 이력·제보자 정보를 조회합니다. 관리자 API는 조회 전용입니다.")
    @GetMapping("/admin/dining/soldout-reports/{reportId}")
    ResponseEntity<AdminDiningReportResponse> getReport(
        @Auth(permit = ADMIN) Integer adminId,
        @Parameter(description = "조회할 제보 ID", example = "1", required = true)
        @PathVariable Integer reportId);
}
