package in.koreatech.koin.admin.coopShop.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopResponse;
import in.koreatech.koin.admin.coopShop.dto.AdminCoopShopsResponse;
import in.koreatech.koin.admin.coopShop.service.AdminCoopShopService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/coopshop")
@RequiredArgsConstructor
public class AdminCoopShopController implements AdminCoopShopApi {

    private final AdminCoopShopService adminCoopShopService;

    @GetMapping
    public ResponseEntity<AdminCoopShopsResponse> getCoopsShops(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @RequestParam(name = "is_deleted", defaultValue = "false") Boolean isDeleted,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminCoopShopsResponse coopShops = adminCoopShopService.getCoopsShops(page, limit, isDeleted);
        return ResponseEntity.ok(coopShops);
    }

    @GetMapping("/{coopShopId}")
    public ResponseEntity<AdminCoopShopResponse> getCoopShop(
        @Auth(permit = {ADMIN}) Integer adminId,
        @PathVariable Integer coopShopId
    ) {
        AdminCoopShopResponse coopShop = adminCoopShopService.getCoopShop(coopShopId);
        return ResponseEntity.ok(coopShop);
    }

}
