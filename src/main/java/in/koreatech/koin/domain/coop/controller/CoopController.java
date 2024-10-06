package in.koreatech.koin.domain.coop.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.LocalDate;

import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coop.dto.CoopLoginRequest;
import in.koreatech.koin.domain.coop.dto.CoopLoginResponse;
import in.koreatech.koin.domain.coop.dto.DiningImageRequest;
import in.koreatech.koin.domain.coop.dto.ExcelResponseBuilder;
import in.koreatech.koin.domain.coop.dto.SoldOutRequest;
import in.koreatech.koin.domain.coop.service.CoopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
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

    @PostMapping("/login")
    public ResponseEntity<CoopLoginResponse> coopLogin(
        @RequestBody @Valid CoopLoginRequest request
    ) {
        CoopLoginResponse response = coopService.coopLogin(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @GetMapping("/dining/excel")
    public ResponseEntity<InputStreamResource> generateCoopExcel(
        @Parameter(description = "시작일 (형식: yyyy-MM-dd)", example = "2024-01-03")
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @Parameter(description = "시작일 (형식: yyyy-MM-dd)", example = "2024-01-10")
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        @RequestParam(name = "isCafeteria", defaultValue = "false") Boolean isCafeteria
    ) {
        ByteArrayInputStream excelFile = coopService.generateDiningExcel(startDate, endDate, isCafeteria);
        return ExcelResponseBuilder.buildExcelResponse(excelFile, startDate, endDate);
    }
}
