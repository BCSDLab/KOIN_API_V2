package in.koreatech.koin.domain.land.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.service.LandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LandController {

    private final LandService landService;

    @GetMapping("/lands")
    public ResponseEntity<List<LandResponse>> getLands() {
        List<LandResponse> responses = landService.getLands();
        return ResponseEntity.ok(responses);
    }
}
