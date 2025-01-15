package in.koreatech.koin.domain.coopshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.dto.CoopShopsResponse;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coopshop")
public class CoopShopController implements CoopShopApi {

    private final CoopShopService coopShopService;

    @GetMapping
    public ResponseEntity<CoopShopsResponse> getCoopShops() {
        CoopShopsResponse coopShops = coopShopService.getCoopShops();
        return ResponseEntity.ok(coopShops);
    }

    @GetMapping("/{coopNameId}")
    public ResponseEntity<CoopShopResponse> getCoopShop(
        @PathVariable Integer coopNameId
    ) {
        CoopShopResponse coopShop = coopShopService.getCoopShop(coopNameId);
        return ResponseEntity.ok(coopShop);
    }
}
