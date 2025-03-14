package in.koreatech.koin.batch.campus.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.batch.campus.bus.city.service.BatchCityBusService;
import in.koreatech.koin.batch.campus.koreatech.dining.service.BatchDiningService;
import in.koreatech.koin.batch.campus.koreatech.service.BatchKoreatechLoginService;
import in.koreatech.koin._common.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch/campus")
public class BatchCampusController implements BatchCampusControllerApi {

    private final BatchCityBusService batchCityBusService;
    private final BatchKoreatechLoginService batchKoreatechLoginService;
    private final BatchDiningService batchDiningService;

    @Override
    @PostMapping("/bus/city")
    public ResponseEntity<Void> updateCityBus(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        batchCityBusService.update();
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/dining")
    public ResponseEntity<Void> updateDining(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        batchKoreatechLoginService.login();
        batchDiningService.update();
        return ResponseEntity.ok().build();
    }
}
