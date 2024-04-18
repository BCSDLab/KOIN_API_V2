package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.service.CoopService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coop")
@RequiredArgsConstructor
public class CoopController implements CoopApi {

    private final CoopService coopService;

    @PatchMapping("/dining/soldout")
    public ResponseEntity<Void> changeSoldOut(
        @Auth(permit = {COOP}) Integer userId,
        @Valid @RequestBody SoldOutRequest soldOutRequest
    ) {
        coopService.changeSoldOut(soldOutRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/dining/image")
    public ResponseEntity<Void> saveDiningImage(
        @Auth(permit = {COOP}) Integer userId,
        @RequestBody @Valid DiningImageRequest imageRequest
    ) {
        coopService.saveDiningImage(imageRequest);
        return ResponseEntity.ok().build();
    }
}
