package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.BOT_AUTHENTICATION_FAILED;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_ALREADY_PROCESSED;
import static in.koreatech.koin.global.code.ApiResponseCode.DINING_REPORT_DELIVERY_CONFLICT;
import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.NO_CONTENT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_FOUND_DINING_REPORT_DELIVERY;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_READABLE_HTTP_MESSAGE;
import static in.koreatech.koin.global.code.ApiResponseCode.OK;
import static in.koreatech.koin.global.code.ApiResponseCode.OPTIMISTIC_LOCKING_FAILURE;

import java.util.Set;

import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import in.koreatech.koin.domain.dining.dto.DiningReportDecisionRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.global.code.ApiResponseCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.validation.Valid;

@Tag(name = "(Bot) Dining: 식단 품절 제보")
@SecurityRequirement(name = "Bot Service Authentication")
public interface DiningReportBotApi {

    @ApiResponseCodes({OK, NO_CONTENT, BOT_AUTHENTICATION_FAILED})
    @ApiResponse(responseCode = "200", description = "실행할 작업 한 건",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningReportDeliveryResponse.class)))
    @ApiResponse(responseCode = "204", description = "수행할 작업이 없으면 본문 없이 반환합니다.", content = @Content,
        headers = @Header(name = "Retry-After", description = "다음 조회까지 기다릴 시간(초)",
            schema = @Schema(type = "integer", allowableValues = "5", example = "5")))
    @Operation(operationId = "claimDiningReportDelivery", summary = "제보 변경 작업 한 건 가져오기", description = """
        - 서비스 토큰으로 인증하며 본문 없이 호출합니다.
        - 작업이 있으면 200을, 없으면 본문 없는 204와 Retry-After: 5를 반환합니다. 5초 뒤 다시 조회합니다.
        - report는 해당 변경의 고정된 내용입니다. 같은 제보의 이전 작업이 완료되어야 다음 변경을 배정합니다.
        - expires_at은 배정 후 60초가 되는 한국시간이며, 이 시각부터 같은 작업을 다시 배정할 수 있습니다.
        - 재배정 시 delivery_id는 유지되고 attempt_token은 새로 발급됩니다. 삐봇은 delivery_id로 중복 처리를 관리합니다.
        - 전송 대상과 메시지 정보, 생성과 수정, 확인은 삐봇이 관리합니다. 반복 배정에 따른 중복 전송 방지도 삐봇에서 처리합니다.
        """, parameters = @Parameter(name = "X-Koin-Service-Token", in = ParameterIn.HEADER, required = true,
            description = "발급받은 서비스 토큰 원문. Bearer는 붙이지 않습니다.", schema = @Schema(type = "string")))
    @PostMapping("/internal/dining/soldout-reports/deliveries/claim")
    ResponseEntity<DiningReportDeliveryResponse> claimDiningReportDelivery();

    @ApiResponseCodes({
        OK, ILLEGAL_ARGUMENT, INVALID_REQUEST_BODY, NOT_READABLE_HTTP_MESSAGE,
        BOT_AUTHENTICATION_FAILED, NOT_FOUND_DINING_REPORT_DELIVERY, DINING_REPORT_DELIVERY_CONFLICT
    })
    @Operation(operationId = "reportDiningReportDeliveryResult", summary = "작업 결과 통보", description = """
        - 본문에는 attempt_token과 outcome(SUCCEEDED 또는 FAILED)만 전달합니다. 다른 필드는 null이어도 거부합니다.
        - SUCCEEDED는 해당 변경 작업을 완료합니다. 제보 승인이나 반려와 별개이며 백엔드가 외부 전송을 확인한 뜻은 아닙니다.
        - FAILED는 작업을 대기 상태로 돌리고 5초 뒤부터 다시 배정할 수 있게 합니다.
        - 현재 토큰은 expires_at이 지나도 재배정 전까지 결과를 접수할 수 있습니다.
        - 마지막으로 접수한 동일 토큰과 결과의 재통보는 200과 현재 delivery_state를 반환합니다.
        - 재배정된 이전 토큰, 현재 시도와 다른 토큰, 이미 접수한 결과와 반대인 결과는 409를 반환합니다.
        - 작업이 없거나 토큰이 다른 작업에 속하는 것으로 확인되면 404를 반환합니다.
        """, parameters = @Parameter(name = "X-Koin-Service-Token", in = ParameterIn.HEADER, required = true,
            description = "발급받은 서비스 토큰 원문. Bearer는 붙이지 않습니다.", schema = @Schema(type = "string")))
    @PostMapping("/internal/dining/soldout-reports/deliveries/{deliveryId}/result")
    ResponseEntity<DiningReportDeliveryResultResponse> reportDiningReportDeliveryResult(
        @Parameter(description = "배정받은 delivery_id", required = true,
            schema = @Schema(type = "string", format = "uuid", pattern = DiningReportDeliveryResultRequest.UUID_PATTERN))
        @PathVariable String deliveryId, @Valid @RequestBody DiningReportDeliveryResultRequest request);

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
        - affected_report_ids로 함께 처리한 제보를 확인합니다. 변경 내용은 다음 작업으로 배정됩니다.
        - already_processed는 업무 처리 재호출 여부입니다.
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
        - 영양사가 먼저 품절 처리한 제보도 기존 COOP_PREPROCESSED, REJECTED 결과를 반환합니다.
        - 변경 내용은 다음 작업으로 배정됩니다.
        """)
    @PostMapping("/internal/dining/soldout-reports/{reportId}/reject")
    ResponseEntity<DiningReportDecisionResponse> rejectReport(
        @Parameter(description = "반려할 제보 ID", example = "1", required = true)
        @PathVariable Integer reportId, @Valid @RequestBody DiningReportDecisionRequest request);

    @ApiResponseCodes({OK, ILLEGAL_ARGUMENT, BOT_AUTHENTICATION_FAILED, NOT_FOUND_DINING_REPORT})
    @SecurityRequirement(name = "Bot Service Authentication")
    @Operation(summary = "삐봇 식단 품절 제보 상세 조회",
        description = """
            - 제보의 식단과 사진, 현재 처리 결과를 조회합니다. 제보자 정보는 반환하지 않습니다.
            - 작업에는 배정받은 report를 사용하며 상세 조회 결과로 고정된 내용을 대체하지 않습니다.
            - 수동 처리에는 processor와 processed_at이 있고 대기 중이거나 자동 처리된 경우에는 null입니다. 자동 처리 시각은 updated_at입니다.
            """)
    @GetMapping("/internal/dining/soldout-reports/{reportId}")
    ResponseEntity<DiningReportResponse> getReport(
        @Parameter(description = "조회할 제보 ID", example = "1", required = true)
        @PathVariable Integer reportId);

    @Component
    class DeliverySchemaCustomizer implements GlobalOpenApiCustomizer {

        @Override
        public void customise(OpenAPI openApi) {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            // 기존 Swagger 생성기는 클래스의 additionalProperties=false 어노테이션을 누락합니다.
            Set<String> closedObjects = Set.of("DiningReportDeliveryResponse", "DiningReportDeliveryResultRequest",
                "DiningReportDeliveryResultResponse");
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (closedObjects.contains(name) && "object".equals(schema.getType())) {
                    schema.setAdditionalProperties(false);
                }
            });
        }
    }
}
