package in.koreatech.koin.domain.dining.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.DiningLikeRequest;
import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.service.DiningService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiningController implements DiningApi {

    private final DiningService diningService;

    @GetMapping("/dinings")
    public ResponseEntity<List<DiningResponse>> getDinings(
        @DateTimeFormat(pattern = "yyMMdd")
        @RequestParam(required = false) LocalDate date
    ) {
        List<DiningResponse> responses = diningService.getDinings(date);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/dining/like")
    public ResponseEntity<Void> likeDining(
        @RequestBody DiningLikeRequest diningLikeRequest
    ) {
        diningService.likeDining(diningLikeRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/dining/like/cancel")
    public ResponseEntity<Void> likeDiningCancel(
        @RequestBody DiningLikeRequest diningLikeRequest
    ) {
        diningService.likeDiningCancel(diningLikeRequest);
        return ResponseEntity.ok().build();
    }
}
