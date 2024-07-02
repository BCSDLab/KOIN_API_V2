package in.koreatech.koin.domain.coopshop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.coopshop.dto.CoopShopResponse;
import in.koreatech.koin.domain.coopshop.service.CoopShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CoopShopController implements CoopShopApi {

    private final CoopShopService coopShopService;

    @GetMapping("/coop")
    public ResponseEntity<List<CoopShopResponse>> getCoopsShops() {
        List<CoopShopResponse> coopShops = coopShopService.getCoopsShops();
        return ResponseEntity.ok(coopShops);
    }

    @GetMapping("/coop/{coopId}")
    public ResponseEntity<CoopShopResponse> getCoopShop(
        @PathVariable Integer coopId
    ) {
        CoopShopResponse coopShop = coopShopService.getCoopShop(coopId);
        return ResponseEntity.ok(coopShop);
    }
}
