package in.koreatech.koin.admin.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.dining.dto.AdminDiningReportResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportPageResponse;
import in.koreatech.koin.domain.dining.dto.DiningReportSummaryResponse;
import in.koreatech.koin.domain.dining.service.DiningReportQueryService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminDiningReportController implements AdminDiningReportApi {

    private final DiningReportQueryService diningReportQueryService;

    @GetMapping("/admin/dining/soldout-reports")
    public ResponseEntity<DiningReportPageResponse<DiningReportSummaryResponse>> getReports(
        @Auth(permit = ADMIN) Integer adminId,
        @RequestParam(name = "only_pending", defaultValue = "false") boolean onlyPending,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(diningReportQueryService.getAdminReports(onlyPending, page, limit));
    }

    @GetMapping("/admin/dining/soldout-reports/{reportId}")
    public ResponseEntity<AdminDiningReportResponse> getReport(
        @Auth(permit = ADMIN) Integer adminId, @PathVariable Integer reportId
    ) {
        return ResponseEntity.ok(diningReportQueryService.getAdminReport(reportId));
    }
}
