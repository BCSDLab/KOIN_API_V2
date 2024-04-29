package in.koreatech.koin.domain.kakao.controller;

import static in.koreatech.koin.domain.kakao.config.KakaoRequestType.DINING;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.kakao.config.KakaoRequest;
import in.koreatech.koin.domain.kakao.service.KakaoBotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/koinbot")
public class KakaoBotController {

    private final KakaoBotService kakaoBotService;

    @PostMapping("/dinings")
    public ResponseEntity<String> requestDinings(
        @RequestBody @KakaoRequest(type = DINING) String diningRequest
    ) {
        var result = kakaoBotService.getDiningMenus(diningRequest);
        return ResponseEntity.ok(result);
    }
}
