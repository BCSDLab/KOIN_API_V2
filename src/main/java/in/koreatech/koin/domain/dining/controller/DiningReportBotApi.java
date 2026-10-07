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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import jakarta.validation.Valid;

@Tag(name = "(Bot) Dining: 식단 품절 제보")
@SecurityRequirement(name = "Bot Service Authentication")
public interface DiningReportBotApi {

    @ApiResponseCodes({OK, NO_CONTENT, BOT_AUTHENTICATION_FAILED})
    @ApiResponse(responseCode = "200", description = "실행할 전송 작업 한 건",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = DiningReportDeliveryResponse.class)))
    @ApiResponse(responseCode = "204", description = "수행할 작업이 없으면 본문 없이 반환합니다.", content = @Content,
        headers = @Header(name = "Retry-After", description = "다음 조회까지 기다릴 시간(초)",
            schema = @Schema(type = "integer", allowableValues = "5", example = "5")))
    @Operation(operationId = "claimDiningReportDelivery", summary = "전송 작업 한 건 가져오기", description = """
        - 인증 헤더를 넣고 본문 없이 호출하면 지금 수행할 수 있는 작업 한 건을 배정합니다. 요청마다 새 작업을 조회합니다.
        - 작업이 있으면 200을, 없으면 본문 없는 204와 Retry-After: 5를 반환합니다. 5초 뒤 다시 조회합니다.
        - SEND일 때만 생성이나 수정을 시도별 최대 한 번 호출합니다. VERIFY는 기존 메시지 확인만 수행합니다.
        - expires_at은 배정 후 60초가 되는 시각입니다. 이후 생성이나 수정 요청을 새로 시작하거나 반복하지 않습니다.
        - 최초 CREATE는 저장이 완료된 최신 제보 상태를 사용합니다. 이미 처리된 제보라도 메시지가 없으면 CREATE입니다.
        - 배정받은 report, operation, target은 바꾸지 않습니다. 이후 변경은 성공 확인 뒤 새 UPDATE로 반영합니다.
        - UPDATE는 저장된 워크스페이스와 채널, message_ts를 그대로 사용합니다. 기존 메시지를 CREATE로 대체하지 않습니다.
        - 첫 블록은 section이며 block_id에 koin_dining_delivery:<delivery_id>를 넣고 전체 내용과 한 번에 전송합니다.
        - VERIFY는 대상, 신뢰할 수 있는 삐봇 작성자 ID, 표식과 내용을 모두 확인하며 UPDATE는 message_ts도 비교합니다.
        - 여러 메시지가 일치하거나 조회 실패, 기한 만료, 요청 제한이나 접근 문제로 확인하지 못하면 UNCERTAIN을 통보합니다.
        - claim 응답을 잃었다면 다음 요청이 같은 작업을 반환한다고 보장하지 않습니다. 기존 시도의 복구 절차를 따릅니다.
        """, parameters = @Parameter(name = "X-Koin-Service-Token", in = ParameterIn.HEADER, required = true,
            description = "발급받은 서비스 토큰 원문. Bearer는 붙이지 않습니다.", schema = @Schema(type = "string")))
    @PostMapping("/internal/dining/soldout-reports/deliveries/claim")
    ResponseEntity<DiningReportDeliveryResponse> claimDiningReportDelivery();

    @ApiResponseCodes({
        OK, ILLEGAL_ARGUMENT, INVALID_REQUEST_BODY, NOT_READABLE_HTTP_MESSAGE,
        BOT_AUTHENTICATION_FAILED, NOT_FOUND_DINING_REPORT_DELIVERY, DINING_REPORT_DELIVERY_CONFLICT
    })
    @Operation(operationId = "reportDiningReportDeliveryResult", summary = "전송 작업 결과 통보", description = """
        - deliveryId와 해당 시도의 attempt_token으로 결과를 통보합니다. 기한이 지난 결과도 확인합니다.
        - SUCCEEDED에는 실제 channel과 문자열 ts를 message_ref로 전달합니다. UPDATE는 기존 메시지 연결과 같아야 합니다.
        - SEND가 반영되지 않았음이 확실한 경우만 NOT_APPLIED입니다. VERIFY에서는 사용할 수 없습니다.
        - NOT_SENT는 요청 미호출, RATE_LIMITED는 1 이상의 정수 retry_after_seconds,
          REJECTED는 1~128자의 error_code를 전달합니다. 각 형식에 없는 필드는 null이어도 전달하지 않습니다.
        - 반영 여부를 알 수 없거나 VERIFY로 확인하지 못했다면 UNCERTAIN을 통보하며 메시지를 다시 쓰지 않습니다.
        - 같은 결과와 메시지 정보를 재통보하면 중복 완료 없이 200과 현재 delivery_state를 반환합니다.
        - 늦게 도착한 확실한 SEND 결과는 유효한 VERIFY를 종료하며 이후 오래된 실패나 만료가 결과를 되돌리지 않습니다.
        - 이미 확인한 결과와 다른 결과나 메시지 연결은 409입니다. 기존 연결을 보존하고 추가 전송을 보류합니다.
        - 작업이 없거나 해당 작업의 attempt_token이 아니면 404를 반환합니다.
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
        - affected_report_ids로 함께 처리한 제보를 확인하고 다음 전송 작업을 가져옵니다. 승인 응답으로 슬랙을 직접 수정하지 않습니다.
        - already_processed는 업무 처리 재호출 여부이며 슬랙 반영 완료를 뜻하지 않습니다.
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
        - 슬랙 메시지는 다음 전송 작업으로 반영합니다. 반려 응답으로 메시지를 직접 수정하거나 전송 완료로 판단하지 않습니다.
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
            - 슬랙 메시지는 배정받은 report로 작성하며 상세 조회 결과로 작업 내용을 바꾸거나 직접 메시지를 수정하지 않습니다.
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
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (name.startsWith("DiningReportDelivery")
                    && ("object".equals(schema.getType()) || schema.getOneOf() != null)) {
                    schema.setAdditionalProperties(false);
                }
            });
            io.swagger.v3.oas.models.media.Schema<?> claim =
                openApi.getComponents().getSchemas().get("DiningReportDeliveryResponse");
            io.swagger.v3.oas.models.media.Schema<?> target =
                openApi.getComponents().getSchemas().get("DiningReportDeliveryMessageTarget");
            if (claim != null && target != null) {
                claim.setOneOf(List.of(claimVariant(claim, target, "CREATE"), claimVariant(claim, target, "UPDATE")));
            }
        }

        private ObjectSchema claimVariant(
            io.swagger.v3.oas.models.media.Schema<?> claim,
            io.swagger.v3.oas.models.media.Schema<?> target,
            String operation
        ) {
            StringSchema messageTs = new StringSchema();
            if ("CREATE".equals(operation)) {
                messageTs.setNullable(true);
                messageTs.setEnum(Collections.singletonList(null));
            } else {
                messageTs.setMinLength(1);
            }

            ObjectSchema targetVariant = new ObjectSchema();
            targetVariant.setProperties(new LinkedHashMap<>(target.getProperties()));
            targetVariant.setRequired(target.getRequired());
            targetVariant.setAdditionalProperties(false);
            targetVariant.addProperty("message_ts", messageTs);

            ObjectSchema variant = new ObjectSchema();
            variant.setTitle(operation);
            variant.setProperties(new LinkedHashMap<>(claim.getProperties()));
            variant.setRequired(claim.getRequired());
            variant.setAdditionalProperties(false);
            variant.addProperty("operation", new StringSchema()._enum(List.of(operation)));
            variant.addProperty("target", targetVariant);
            return variant;
        }
    }
}
