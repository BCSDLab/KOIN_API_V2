package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.service.DiningService;
import in.koreatech.koin.global.auth.Auth;
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
        @Auth (permit = {STUDENT, COOP}) Integer userId,
        @RequestParam Integer diningId
    ) {
        diningService.likeDining(userId, diningId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/dining/like/cancel")
    public ResponseEntity<Void> likeDiningCancel(
        @Auth (permit = {STUDENT, COOP}) Integer userId,
        @RequestParam Integer diningId
    ) {
        diningService.likeDiningCancel(userId, diningId);
        return ResponseEntity.ok().build();
    }
}
