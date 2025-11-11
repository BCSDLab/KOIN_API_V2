package in.koreatech.koin.admin.bus.shuttle.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.shuttle.dto.response.AdminShuttleBusTimeTableResponse;
import in.koreatech.koin.admin.bus.shuttle.service.AdminShuttleBusTimeTableService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/bus/shuttle")
@RequiredArgsConstructor
public class AdminShuttleBusTimeTableController implements AdminShuttleBusTimeTableApi {

    private final AdminShuttleBusTimeTableService adminShuttleBusTimeTableService;

    @PostMapping("/preview")
    public ResponseEntity<List<AdminShuttleBusTimeTableResponse>> previewShuttleBusTimeTable(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(name = "shuttle-bus-time-table") MultipartFile file
    ) {
        List<AdminShuttleBusTimeTableResponse> response = adminShuttleBusTimeTableService.previewShuttleBusTimeTable(file);

        return ResponseEntity.ok(response);
    }
}
