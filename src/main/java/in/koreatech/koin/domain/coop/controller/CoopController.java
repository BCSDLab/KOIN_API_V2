package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.time.LocalDate;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.coop.dto.CompressFileResponseBuilder;
import in.koreatech.koin.domain.coop.dto.CoopLoginRequest;
import in.koreatech.koin.domain.coop.dto.CoopLoginResponse;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.ExcelResponseBuilder;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.service.CoopService;
import in.koreatech.koin.domain.coop.dto.CoopResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CoopController implements CoopApi {

    private final CoopService coopService;

    @PatchMapping("/coop/dining/soldout")
    public ResponseEntity<Void> changeSoldOut(
        @Auth(permit = {COOP}) Integer userId,
        @Valid @RequestBody SoldOutRequest soldOutRequest
    ) {
        coopService.changeSoldOut(soldOutRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/coop/dining/image")
    public ResponseEntity<Void> saveDiningImage(
        @Auth(permit = {COOP}) Integer userId,
        @RequestBody @Valid DiningImageRequest imageRequest
    ) {
        coopService.saveDiningImage(imageRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/coop/login")
    public ResponseEntity<CoopLoginResponse> coopLogin(
        @RequestBody @Valid CoopLoginRequest request
    ) {
        CoopLoginResponse response = coopService.coopLogin(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @GetMapping("/user/coop/me")
    public ResponseEntity<CoopResponse> getCoop(
        @Auth(permit = COOP) Integer userId
    ) {
        CoopResponse coopResponse = coopService.getCoop(userId);
        return ResponseEntity.ok().body(coopResponse);
    }

    @GetMapping("/coop/dining/excel")
    public ResponseEntity<InputStreamResource> generateCoopExcel(
        @Auth(permit = {COOP}) Integer userId,
        @Parameter(description = "시작일 (형식: yyyy-MM-dd)", example = "2022-11-29")
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "시작일 (형식: yyyy-MM-dd)", example = "2023-01-10")
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        @RequestParam(name = "isCafeteria", defaultValue = "false") Boolean isCafeteria
    ) {
        ByteArrayInputStream excelFile = coopService.generateDiningExcel(startDate, endDate, isCafeteria);
        return ExcelResponseBuilder.buildExcelResponse(excelFile, startDate, endDate);
    }

    @GetMapping("/coop/dining/image")
    public ResponseEntity<Resource> generateImageCompress(
        @Auth(permit = {COOP}) Integer userId,
        @Parameter(description = "시작일 (형식: yyyy-MM-dd)", example = "2022-11-29")
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "종료일 (형식: yyyy-MM-dd)", example = "2023-01-10")
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        @RequestParam(name = "isCafeteria", defaultValue = "false") Boolean isCafeteria
    ) {
        File zipFile = coopService.generateDiningImageCompress(startDate, endDate, isCafeteria);
        ResponseEntity<Resource> response = CompressFileResponseBuilder.buildCompressFileResponse(zipFile);
        coopService.removeDiningImageCompress(zipFile);
        return response;
    }
}
