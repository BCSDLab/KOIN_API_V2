package in.koreatech.koin.admin.track.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.track.dto.AdminTrackResponse;
import in.koreatech.koin.admin.track.service.AdminTrackService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminTrackController implements AdminTrackApi {

    private final AdminTrackService adminTrackService;

    @GetMapping("/admin/tracks")
    public ResponseEntity<List<AdminTrackResponse>> getTracks(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        List<AdminTrackResponse> adminTrackResponse = adminTrackService.getTracks();
        return ResponseEntity.ok(adminTrackResponse);
    }
}
