package in.koreatech.koin.domain.dining.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.dining.dto.SoldOutRequest;
import in.koreatech.koin.domain.dining.service.CoopDiningService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CoopDiningController implements CoopDiningApi{

    private final CoopDiningService coopDiningService;

    @PatchMapping("/coop/dining/soldout")
    public ResponseEntity<Void> changeSoldOut(
        @Auth(permit = {COOP}) Long userId,
        @RequestBody SoldOutRequest soldOutRequest
    ) {
        coopDiningService.changeSoldOut(soldOutRequest);
        return ResponseEntity.ok().build();
    }
}
