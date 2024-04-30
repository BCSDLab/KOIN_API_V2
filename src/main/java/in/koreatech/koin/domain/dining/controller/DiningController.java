package in.koreatech.koin.domain.dining.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
