package in.koreatech.koin.domain.benefit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.benefit.dto.BenefitCategoryResponse;
import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;
import in.koreatech.koin.domain.benefit.service.ShopBenefitService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BenefitController implements BenefitApi {

    private final ShopBenefitService shopBenefitService;

    @GetMapping("/benefit/categories")
    public ResponseEntity<BenefitCategoryResponse> getBenefitCategories() {
        BenefitCategoryResponse benefitCategoryResponse = shopBenefitService.getBenefitCategories();
        return ResponseEntity.ok(benefitCategoryResponse);
    }

    @GetMapping("/benefit/{id}/shops")
    public ResponseEntity<BenefitShopsResponse> getBenefitShops(
        @PathParam("id") Integer benefitId
    ) {
        BenefitShopsResponse benefitShopsResponse = shopBenefitService.getBenefitShops(benefitId);
        return ResponseEntity.ok(benefitShopsResponse);
    }
}
