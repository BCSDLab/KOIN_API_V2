package in.koreatech.koin.domain.dining.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.DiningReportChangesResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionRequest;
import in.koreatech.koin.domain.dining.dto.DiningReportDecisionResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportPageResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportResponse;
import in.koreatech.koin.domain.dining.model.DiningReportStatus;
import in.koreatech.koin.domain.dining.service.DiningReportQueryService;
import in.koreatech.koin.domain.dining.service.DiningReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiningReportBotController implements DiningReportBotApi {

    private final DiningReportService diningReportService;
    private final DiningReportQueryService diningReportQueryService;

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

    @GetMapping("/internal/dining/soldout-reports")
    public ResponseEntity<DiningReportPageResponse<DiningReportResponse>> getReports(
        @RequestParam(name = "dining_id", required = false) Integer diningId,
        @RequestParam(name = "processing_id", required = false) UUID processingId,
        @RequestParam(required = false) DiningReportStatus status,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(diningReportQueryService.getBotReports(diningId, processingId, status, page, limit));
    }

    @GetMapping("/internal/dining/soldout-reports/changes")
    public ResponseEntity<DiningReportChangesResponse> getChanges(
        @RequestParam(defaultValue = "0") String cursor, @RequestParam(defaultValue = "50") Integer limit
    ) {
        return ResponseEntity.ok(diningReportQueryService.getChanges(cursor, limit));
    }
}
