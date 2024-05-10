package in.koreatech.koin.admin.track.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.track.dto.AdminTrackResponse;
import in.koreatech.koin.admin.track.service.AdminTrackService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminTrackController implements AdminTrackApi {

    private final AdminTrackService adminTrackService;

    @GetMapping("/admin/tracks")
    public ResponseEntity<List<AdminTrackResponse>> getTracks() {
        List<AdminTrackResponse> adminTrackResponse = adminTrackService.getTracks();
        return ResponseEntity.ok(adminTrackResponse);
    }
}
