package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.service.CoopService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CoopController implements CoopApi {

    private final CoopService coopDiningService;

    @PatchMapping("/coop/dining/image")
    public ResponseEntity<Void> saveDiningImage(
        @Auth(permit = {COOP}) Long userId,
        @RequestBody DiningImageRequest imageRequest
    ) {
        coopDiningService.saveDiningImage(imageRequest);
        return ResponseEntity.ok().build();
    }
}
