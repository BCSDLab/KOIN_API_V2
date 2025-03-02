package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.DiningResponse;
import in.koreatech.koin.domain.dining.dto.DiningSearchResponse;
import in.koreatech.koin.domain.dining.model.DiningPlace;
import in.koreatech.koin.domain.dining.service.DiningService;
import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin._common.auth.UserId;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DiningController implements DiningApi {

    private final DiningService diningService;

    @GetMapping("/dinings")
    public ResponseEntity<List<DiningResponse>> getDinings(
        @DateTimeFormat(pattern = "yyMMdd")
        @RequestParam(required = false) LocalDate date,
        @UserId Integer userId
    ) {
        List<DiningResponse> responses = diningService.getDinings(date, userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/dining/like")
    public ResponseEntity<Void> likeDining(
        @Auth(permit = {STUDENT, COOP, COUNCIL}) Integer userId,
        @RequestParam Integer diningId
    ) {
        diningService.likeDining(userId, diningId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/dining/like/cancel")
    public ResponseEntity<Void> likeDiningCancel(
        @Auth(permit = {STUDENT, COOP, COUNCIL}) Integer userId,
        @RequestParam Integer diningId
    ) {
        diningService.likeDiningCancel(userId, diningId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dinings/search")
    public ResponseEntity<DiningSearchResponse> searchDinings(
        @Auth(permit = {COOP}) Integer userId,
        @RequestParam String keyword,
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(required = false) List<DiningPlace> filter
    ) {
        DiningSearchResponse diningSearchResponse = diningService.searchDinings(keyword, page, limit, filter);
        return ResponseEntity.ok().body(diningSearchResponse);
    }
}
