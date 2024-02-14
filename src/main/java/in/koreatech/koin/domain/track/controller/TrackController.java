package in.koreatech.koin.domain.track.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.track.dto.TrackResponse;
import in.koreatech.koin.domain.track.dto.TrackSingleResponse;
import in.koreatech.koin.domain.track.service.TrackService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TrackController implements TrackApi {

    private final TrackService trackService;

    @GetMapping("/tracks")
    public ResponseEntity<List<TrackResponse>> getTracks() {
        List<TrackResponse> tracksResponse = trackService.getTracks();
        return ResponseEntity.ok(tracksResponse);
    }

    @GetMapping("/tracks/{id}")
    public ResponseEntity<TrackSingleResponse> getTrack(
        @PathVariable Long id
    ) {
        TrackSingleResponse response = trackService.getTrack(id);
        return ResponseEntity.ok(response);
    }
}
