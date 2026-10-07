package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.global.code.ApiResponseCode.ILLEGAL_ARGUMENT;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.DiningReportDecisionRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDeliveryResultResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.service.DiningReportDeliveryService;
import in.koreatech.koin.domain.dining.service.DiningReportQueryService;
import in.koreatech.koin.domain.dining.service.DiningReportService;
import in.koreatech.koin.global.exception.CustomException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiningReportBotController implements DiningReportBotApi {

    private final DiningReportService diningReportService;
    private final DiningReportQueryService diningReportQueryService;
    private final DiningReportDeliveryService diningReportDeliveryService;

    @PostMapping("/internal/dining/soldout-reports/deliveries/claim")
    public ResponseEntity<DiningReportDeliveryResponse> claimDiningReportDelivery() {
        return diningReportDeliveryService.claim()
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.noContent().header(HttpHeaders.RETRY_AFTER, "5").build());
    }

    @PostMapping("/internal/dining/soldout-reports/deliveries/{deliveryId}/result")
    public ResponseEntity<DiningReportDeliveryResultResponse> reportDiningReportDeliveryResult(
        @PathVariable String deliveryId, @Valid @RequestBody DiningReportDeliveryResultRequest request
    ) {
        if (!deliveryId.matches(DiningReportDeliveryResultRequest.UUID_PATTERN)) {
            throw CustomException.of(ILLEGAL_ARGUMENT);
        }
        return ResponseEntity.ok(diningReportDeliveryService.recordResult(UUID.fromString(deliveryId), request));
    }

    @PostMapping("/internal/dining/soldout-reports/{reportId}/approve")
    public ResponseEntity<DiningReportDecisionResponse> approveReport(
        @PathVariable Integer reportId, @Valid @RequestBody DiningReportDecisionRequest request
    ) {
        return ResponseEntity.ok(diningReportService.approve(reportId, request.actor()));
    }

    @PostMapping("/internal/dining/soldout-reports/{reportId}/reject")
    public ResponseEntity<DiningReportDecisionResponse> rejectReport(
        @PathVariable Integer reportId, @Valid @RequestBody DiningReportDecisionRequest request
    ) {
        return ResponseEntity.ok(diningReportService.reject(reportId, request.actor()));
    }

    @GetMapping("/internal/dining/soldout-reports/{reportId}")
    public ResponseEntity<DiningReportResponse> getReport(@PathVariable Integer reportId) {
        return ResponseEntity.ok(diningReportQueryService.getBotReport(reportId));
    }
}
