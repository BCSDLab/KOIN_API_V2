package in.koreatech.koin.admin.bus.shuttle.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.SHUTTLE_BUS;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.commuting.enums.SemesterType;
import in.koreatech.koin.admin.bus.shuttle.dto.request.AdminShuttleBusUpdateRequest;
import in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimetableResponse;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusExcelService;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusService;
import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/bus/shuttle/timetable")
@RequiredArgsConstructor
public class AdminShuttleBusTimetableController implements AdminShuttleBusTimetableApi {

    private final AdminShuttleBusExcelService adminShuttleBusExcelService;
    private final AdminShuttleBusService adminShuttleBusService;

    @AdminActivityLogging(domain = SHUTTLE_BUS)
    @PostMapping(value = "/excel", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminShuttleBusTimetableResponse> uploadTimetableExcelForPreview(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(name = "shuttle-bus-timetable") MultipartFile file
    ) {
        AdminShuttleBusTimetableResponse response = adminShuttleBusExcelService
            .getShuttleBusTimetablePreview(file);

        return ResponseEntity.ok(response);
    }

    @AdminActivityLogging(domain = SHUTTLE_BUS)
    @PutMapping
    public ResponseEntity<Void> updateShuttleBusTimetable(
        @RequestParam(name = "semester_type") SemesterType semesterType,
        @Valid @RequestBody AdminShuttleBusUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShuttleBusService.updateShuttleBusTimetable(request, semesterType);

        return ResponseEntity.ok().build();
    }
}
