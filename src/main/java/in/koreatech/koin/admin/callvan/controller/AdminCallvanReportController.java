package in.koreatech.koin.admin.callvan.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.callvan.dto.AdminCallvanReportProcessRequest;
import in.koreatech.koin.admin.callvan.dto.AdminCallvanReportsResponse;
import in.koreatech.koin.admin.callvan.service.AdminCallvanReportQueryService;
import in.koreatech.koin.admin.callvan.service.AdminCallvanReportService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/callvan/reports")
@RequiredArgsConstructor
public class AdminCallvanReportController implements AdminCallvanReportApi {

    private final AdminCallvanReportService adminCallvanReportService;
    private final AdminCallvanReportQueryService adminCallvanReportQueryService;

    @GetMapping
    public ResponseEntity<AdminCallvanReportsResponse> getCallvanReports(
        @RequestParam(name = "only_pending", required = false, defaultValue = "false") Boolean onlyPending,
        @RequestParam(name = "page", required = false) Integer page,
        @RequestParam(name = "limit", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok(adminCallvanReportQueryService.getReports(onlyPending, page, limit));
    }

    @PostMapping("/{reportId}/process")
    public ResponseEntity<Void> processCallvanReport(
        @Parameter(in = PATH) @PathVariable Integer reportId,
        @RequestBody @Valid AdminCallvanReportProcessRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminCallvanReportService.processReport(reportId, adminId, request);
        return ResponseEntity.ok().build();
    }
}
