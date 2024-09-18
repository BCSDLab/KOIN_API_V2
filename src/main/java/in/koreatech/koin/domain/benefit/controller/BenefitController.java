package in.koreatech.koin.domain.benefit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.benefit.dto.BenefitCategoryResponse;
import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;
import in.koreatech.koin.domain.benefit.service.ShopBenefitService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/benefit")
public class BenefitController implements BenefitApi {

    private final ShopBenefitService shopBenefitService;

    @GetMapping("/categories")
    public ResponseEntity<BenefitCategoryResponse> getBenefitCategories() {
        BenefitCategoryResponse benefitCategoryResponse = shopBenefitService.getBenefitCategories();
        return ResponseEntity.ok(benefitCategoryResponse);
    }

    @GetMapping("/{id}/shops")
    public ResponseEntity<BenefitShopsResponse> getBenefitShops(
        @PathVariable("id") Integer benefitId
    ) {
        BenefitShopsResponse benefitShopsResponse = shopBenefitService.getBenefitShops(benefitId);
        return ResponseEntity.ok(benefitShopsResponse);
    }
}
