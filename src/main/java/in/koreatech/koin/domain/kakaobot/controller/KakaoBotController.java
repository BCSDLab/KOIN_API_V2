package in.koreatech.koin.domain.kakaobot.controller;

import static in.koreatech.koin.domain.kakaobot.model.KakaoRequestType.BUS_TIME;
import static in.koreatech.koin.domain.kakaobot.model.KakaoRequestType.DINING;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.kakaobot.web.KakaoRequest;
import in.koreatech.koin.domain.kakaobot.dto.KakaoBusRequest;
import in.koreatech.koin.domain.kakaobot.dto.KakaoDiningRequest;
import in.koreatech.koin.domain.kakaobot.service.KakaoBotService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/koinbot")
public class KakaoBotController {

    private final KakaoBotService kakaoBotService;

    @PostMapping(value = "/dinings", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> requestDinings(
        @RequestBody @KakaoRequest(type = DINING) KakaoDiningRequest diningRequest
    ) {
        var result = kakaoBotService.getDiningMenus(diningRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/buses", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> requestBusTimes(
        @RequestBody @KakaoRequest(type = BUS_TIME) KakaoBusRequest request
    ) {
        var result = kakaoBotService.getBusRemainTime(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/buses/request", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> requestBusRoutes() {
        var result = kakaoBotService.getBusRoutes();
        return ResponseEntity.ok(result);
    }
}
