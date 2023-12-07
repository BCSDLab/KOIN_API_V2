package in.koreatech.koin.controller;

import in.koreatech.koin.dto.TrackResponse;
import in.koreatech.koin.dto.TrackSingleResponse;
import in.koreatech.koin.service.TrackService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @GetMapping("/tracks")
    public ResponseEntity<List<TrackResponse>> getTracks() {
        List<TrackResponse> tracksResponse = trackService.getTracks();
        return ResponseEntity.ok(tracksResponse);
    }

    @GetMapping("/tracks/{id}")
    public ResponseEntity<TrackSingleResponse> getTrack(@PathVariable Long id) {
        TrackSingleResponse response = trackService.getTrack(id);
        return ResponseEntity.ok(response);
    }
}
