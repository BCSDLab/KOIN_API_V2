package in.koreatech.koin.batch.campus.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.batch.campus.bus.city.service.BatchCityBusService;
import in.koreatech.koin.batch.campus.bus.school.dto.BatchSchoolBusVersionUpdateRequest;
import in.koreatech.koin.batch.campus.bus.school.service.SchoolBusService;
import in.koreatech.koin.batch.campus.koreatech.dining.service.BatchDiningService;
import in.koreatech.koin.batch.campus.service.BatchPortalLoginService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch/campus")
public class BatchCampusController implements BatchCampusControllerApi {

    private final SchoolBusService schoolBusService;
    private final BatchCityBusService batchCityBusService;
    private final BatchPortalLoginService batchPortalLoginService;
    private final BatchDiningService batchDiningService;

    @Override
    @PostMapping("/bus/school")
    public ResponseEntity<Void> updateSchoolBus(
        @Auth(permit = {ADMIN}) Integer adminId,
        @RequestBody @Valid BatchSchoolBusVersionUpdateRequest request
    ) {
        schoolBusService.update(request);
        return ResponseEntity.ok().build();
    }

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
        batchPortalLoginService.login();
        batchDiningService.update();
        return ResponseEntity.ok().build();
    }
}
