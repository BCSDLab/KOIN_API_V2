package in.koreatech.koin.admin.coopShop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemesterResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopSemestersResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse;
import in.koreatech.koin.admin.coopShop.service.AdminCoopShopExcelService;
import in.koreatech.koin.admin.coopShop.service.AdminCoopShopService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/coopshop")
@RequiredArgsConstructor
public class AdminCoopShopController implements AdminCoopShopApi {

    private final AdminCoopShopService adminCoopShopService;
    private final AdminCoopShopExcelService adminCoopShopExcelService;

    @GetMapping
    public ResponseEntity<AdminCoopSemestersResponse> getCoopShopSemesters(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminCoopSemestersResponse coopShops = adminCoopShopService.getCoopShopSemesters(page, limit);
        return ResponseEntity.ok(coopShops);
    }

    @GetMapping("/{semesterId}")
    public ResponseEntity<AdminCoopSemesterResponse> getCoopShops(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable Integer semesterId
    ) {
        AdminCoopSemesterResponse coopShop = adminCoopShopService.getCoopShopSemester(semesterId);
        return ResponseEntity.ok(coopShop);
    }

    @PostMapping(value = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminCoopShopsResponse> parseExcel(
        @RequestParam("file") MultipartFile file
    ) {
        AdminCoopShopsResponse data = adminCoopShopExcelService.parse(file);
        return ResponseEntity.ok(data);
    }
}
