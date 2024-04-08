package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import org.springframework.web.bind.annotation.RequestMapping;

import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.service.CoopService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/coop")
@RequiredArgsConstructor
public class CoopController implements CoopApi {

    private final CoopService coopService;
    private final NotificationService notificationService;

    @PatchMapping("/dining/soldout")
    public ResponseEntity<Void> changeSoldOut(
        @Auth(permit = {COOP}) Long userId,
        @Valid @RequestBody SoldOutRequest soldOutRequest
    ) {
        coopService.changeSoldOut(soldOutRequest);
        notificationService.pushSoldOutNotification();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/dining/image")
    public ResponseEntity<Void> saveDiningImage(
        @Auth(permit = {COOP}) Long userId,
        @RequestBody DiningImageRequest imageRequest
    ) {
        coopService.saveDiningImage(imageRequest);
        return ResponseEntity.ok().build();
    }
}
