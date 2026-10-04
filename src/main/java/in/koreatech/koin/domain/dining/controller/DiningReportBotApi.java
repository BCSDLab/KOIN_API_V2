package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.BOT_AUTHENTICATION_FAILED;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_ALREADY_PROCESSED;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_STATE;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_CHANGE_CURSOR;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REPORT_FILTER;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.OPTIMISTIC_LOCKING_FAILURE;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.domain.dining.dto.DiningReportChangesResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportPageResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "(Bot) Dining: 식단 품절 제보 처리·조회")
@SecurityRequirement(name = "Bot Service Authentication")
public interface DiningReportBotApi {

    @ApiResponseCodes({
        OK, ILLEGAL_ARGUMENT, INVALID_REQUEST_BODY, NOT_READABLE_HTTP_MESSAGE,
        BOT_AUTHENTICATION_FAILED, NOT_FOUND_DINING_REPORT, NOT_FOUND_DINING,
        DINING_REPORT_ALREADY_PROCESSED, OPTIMISTIC_LOCKING_FAILURE
    })
    @SecurityRequirement(name = "Bot Service Authentication")
    @Operation(summary = "식단 품절 제보 승인", description = """
        - 서비스 토큰으로 인증하며 actor는 처리자 감사 기록에만 사용합니다.
        - 대상 제보를 승인하면 같은 식단의 PENDING 제보만 같은 처리 묶음으로 자동 승인하고 식단을 품절 처리합니다.
        - 대상에 처리자를 기록하고 자동 승인된 제보는 대상의 source_report_id를 기록합니다.
        - 이미 승인된 제보를 재호출하면 최초 처리 결과·처리자·시각·처리 묶음을 유지합니다. 이미 반려되었다면 409를 반환합니다.
        - 영양사가 먼저 품절 처리하면 해당 식단의 PENDING 제보는 즉시 자동 반려됩니다.
        """)
    @PostMapping("/internal/dining/soldout-reports/{reportId}/approve")
    ResponseEntity<DiningReportDecisionResponse> approveReport(
        @Parameter(description = "승인할 제보 ID", example = "1", required = true)
        @PathVariable Integer reportId, @Valid @RequestBody DiningReportDecisionRequest request);

    @ApiResponseCodes({
        OK, ILLEGAL_ARGUMENT, INVALID_REQUEST_BODY, NOT_READABLE_HTTP_MESSAGE,
        BOT_AUTHENTICATION_FAILED, NOT_FOUND_DINING_REPORT, NOT_FOUND_DINING,
        DINING_REPORT_ALREADY_PROCESSED, OPTIMISTIC_LOCKING_FAILURE
    })
    @SecurityRequirement(name = "Bot Service Authentication")
    @Operation(summary = "식단 품절 제보 반려", description = """
        - 서비스 토큰으로 인증하며 actor는 처리자 감사 기록에만 사용합니다.
        - 대상 PENDING 제보만 반려하고 다른 제보와 식단의 품절 상태를 유지합니다.
        - 이미 반려된 제보를 재호출하면 최초 처리 결과·처리자·시각·처리 묶음을 유지합니다. 이미 승인되었다면 409를 반환합니다.
        """)
    @PostMapping("/internal/dining/soldout-reports/{reportId}/reject")
    ResponseEntity<DiningReportDecisionResponse> rejectReport(
        @Parameter(description = "반려할 제보 ID", example = "1", required = true)
        @PathVariable Integer reportId, @Valid @RequestBody DiningReportDecisionRequest request);

    @ApiResponseCodes({OK, ILLEGAL_ARGUMENT, BOT_AUTHENTICATION_FAILED, NOT_FOUND_DINING_REPORT})
    @SecurityRequirement(name = "Bot Service Authentication")
    @Operation(summary = "삐봇 식단 품절 제보 상세 조회",
        description = "제보의 식단·사진·처리 결과를 조회합니다. 제보자 정보는 반환하지 않습니다.")
    @GetMapping("/internal/dining/soldout-reports/{reportId}")
    ResponseEntity<DiningReportResponse> getReport(
        @Parameter(description = "조회할 제보 ID", example = "1", required = true)
        @PathVariable Integer reportId);

    @ApiResponseCodes({OK, ILLEGAL_ARGUMENT, INVALID_REPORT_FILTER, BOT_AUTHENTICATION_FAILED})
    @SecurityRequirement(name = "Bot Service Authentication")
    @Operation(summary = "삐봇 관련 제보 목록 조회", description = """
        - dining_id와 processing_id 중 정확히 하나를 전달합니다. 둘 다 전달하거나 둘 다 생략하면 400을 반환합니다.
        - status를 전달하면 해당 상태만 조회하며, 제보 ID 오름차순으로 반환합니다.
        - 조건에 맞는 제보가 없으면 빈 목록을 반환합니다. 제보자 정보는 반환하지 않습니다.
        """)
    @GetMapping("/internal/dining/soldout-reports")
    ResponseEntity<DiningReportPageResponse<DiningReportResponse>> getReports(
        @Parameter(description = "식단 ID. processing_id와 둘 중 하나만 필수입니다.", example = "1",
            schema = @Schema(minimum = "1"))
        @RequestParam(name = "dining_id", required = false) Integer diningId,
        @Parameter(description = "처리 묶음 UUID. dining_id와 둘 중 하나만 필수입니다.",
            example = "e924c7d3-3757-4cd0-961b-e65c1f6cc8ad", schema = @Schema(type = "string", format = "uuid"))
        @RequestParam(name = "processing_id", required = false) UUID processingId,
        @Parameter(description = "제보 상태. 생략하면 모든 상태를 조회합니다.", example = "PENDING")
        @RequestParam(required = false) DiningReportStatus status,
        @Parameter(description = "페이지 번호. 1부터 시작하며 전체 페이지 범위로 보정합니다.", example = "1",
            schema = @Schema(defaultValue = "1"))
        @RequestParam(defaultValue = "1") Integer page,
        @Parameter(description = "페이지당 제보 수. 1~50으로 보정합니다.", example = "10",
            schema = @Schema(defaultValue = "10"))
        @RequestParam(defaultValue = "10") Integer limit);

    @ApiResponseCodes({OK, ILLEGAL_ARGUMENT, INVALID_CHANGE_CURSOR, ILLEGAL_STATE, BOT_AUTHENTICATION_FAILED})
    @SecurityRequirement(name = "Bot Service Authentication")
    @Operation(summary = "삐봇 식단 품절 제보 변경 조회", description = """
        - cursor 이후 커밋된 접수·처리 변경을 sequence 오름차순으로 조회합니다. 최초 조회는 문자열 \"0\"을 전달합니다.
        - sequence와 cursor는 정밀도를 보존하는 숫자 문자열입니다. 현재 최종 sequence보다 큰 cursor는 400을 반환합니다.
        - next_cursor로 이어서 조회합니다. 변경이 없으면 요청 cursor를 유지하며, 조회 주기는 클라이언트가 정합니다.
        """)
    @GetMapping("/internal/dining/soldout-reports/changes")
    ResponseEntity<DiningReportChangesResponse> getChanges(
        @Parameter(description = "마지막 수신 sequence. 0~9223372036854775807 범위의 숫자 문자열입니다.",
            example = "0", schema = @Schema(type = "string", pattern = "^[0-9]{1,19}$", defaultValue = "0"))
        @RequestParam(defaultValue = "0") String cursor,
        @Parameter(description = "최대 변경 수. 1~100으로 보정합니다.", example = "50",
            schema = @Schema(defaultValue = "50"))
        @RequestParam(defaultValue = "50") Integer limit);
}
