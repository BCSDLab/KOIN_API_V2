package in.koreatech.koin.domain.coopshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.dto.CoopShopsResponse;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CoopShopController implements CoopShopApi {

    private final CoopShopService coopShopService;

    @GetMapping("/coopshop")
    public ResponseEntity<CoopShopsResponse> getCoopShops() {
        CoopShopsResponse coopShops = coopShopService.getCoopShops();
        return ResponseEntity.ok(coopShops);
    }

    @GetMapping("/coopshop/{coopShopId}")
    public ResponseEntity<CoopShopResponse> getCoopShop(
        @PathVariable Integer coopShopId
    ) {
        CoopShopResponse coopShop = coopShopService.getCoopShop(coopShopId);
        return ResponseEntity.ok(coopShop);
    }
}
