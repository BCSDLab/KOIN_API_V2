package in.koreatech.koin.domain.land.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.land.dto.LandResponse;
import in.koreatech.koin.domain.land.dto.LandsResponse;
import in.koreatech.koin.domain.land.service.LandService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LandController implements LandApi {

    private final LandService landService;

    @GetMapping("/lands")
    public ResponseEntity<List<LandsResponse>> getLands() {
        List<LandsResponse> responses = landService.getLands();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/lands/{id}")
    public ResponseEntity<LandResponse> getLand(
        @PathVariable Long id
    ) {
        LandResponse response = landService.getLand(id);
        return ResponseEntity.ok(response);
    }
}
