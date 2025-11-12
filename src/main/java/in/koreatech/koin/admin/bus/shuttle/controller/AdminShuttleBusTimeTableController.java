package in.koreatech.koin.admin.bus.shuttle.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

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
import in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimeTableResponse;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusExcelService;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/bus/shuttle/timetable")
@RequiredArgsConstructor
public class AdminShuttleBusTimeTableController implements AdminShuttleBusTimeTableApi {

    private final AdminShuttleBusExcelService adminShuttleBusExcelService;
    private final AdminShuttleBusService adminShuttleBusService;

    @PostMapping("/excel")
    public ResponseEntity<List<AdminShuttleBusTimeTableResponse>> previewShuttleBusTimeTable(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(name = "shuttle-bus-time-table") MultipartFile file
    ) {
        List<AdminShuttleBusTimeTableResponse> response = adminShuttleBusExcelService
            .previewShuttleBusTimeTable(file);

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Void> updateShuttleBusTimeTable(
        @RequestParam(name = "semester_type") SemesterType semesterType,
        @Valid @RequestBody AdminShuttleBusUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminShuttleBusService.updateShuttleBusTimeTable(request, semesterType);

        return ResponseEntity.ok().build();
    }
}
