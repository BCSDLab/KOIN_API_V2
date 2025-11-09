package in.koreatech.koin.admin.bus.shuttle.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.bus.shuttle.dto.responose.AdminShuttleBueTimeTableResponse;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/bus/shuttle")
@RequiredArgsConstructor
public class AdminShuttleBusTimeTableController implements AdminShuttleBusTimeTableApi {

    @PostMapping("/preview")
    public ResponseEntity<AdminShuttleBueTimeTableResponse> previewShuttleBusTimeTable(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestParam(value = "file") MultipartFile file
    ) {
        // TODO 업로드된 엑셀 데이터 파싱 후 반환하는 기능 구현

        return ResponseEntity.ok(null);
    }
}
