package in.koreatech.koin.domain.shop.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;
import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.shop.dto.search.response.RelatedKeywordResponse;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopCategoriesResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopSummaryResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponse;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV2;
import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV3;
import in.koreatech.koin.domain.shop.service.ShopSearchService;
import in.koreatech.koin.domain.shop.service.ShopService;
import in.koreatech.koin.global.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShopController implements ShopApi {

    private final ShopService shopService;
    private final ShopSearchService shopSearchService;

    @GetMapping("/shops/{id}")
    public ResponseEntity<ShopResponse> getShopById(
        @PathVariable Integer id
    ) {
        ShopResponse shopResponse = shopService.getShop(id);
        return ResponseEntity.ok(shopResponse);
    }

    @GetMapping("/shops/{id}/summary")
    public ResponseEntity<ShopSummaryResponse> getShopSummary(
        @PathVariable Integer id
    ) {
        ShopSummaryResponse response = shopService.getShopSummary(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops")
    public ResponseEntity<ShopsResponse> getShops() {
        ShopsResponse shopsResponse = shopService.getShops();
        return ResponseEntity.ok(shopsResponse);
    }

    @GetMapping("/shops/categories")
    public ResponseEntity<ShopCategoriesResponse> getShopsCategories() {
        ShopCategoriesResponse shopCategoriesResponse = shopService.getShopsCategories();
        return ResponseEntity.ok(shopCategoriesResponse);
    }

    @GetMapping("/v2/shops")
    public ResponseEntity<ShopsResponseV2> getShopsV2(
        @RequestParam(name = "sorter", defaultValue = "NONE") ShopsSortCriteria sortBy,
        @RequestParam(name = "filter", required = false) List<ShopsFilterCriteria> shopsFilterCriterias,
        @RequestParam(name = "query", required = false, defaultValue = "") String query
    ) {
        if (shopsFilterCriterias == null) {
            shopsFilterCriterias = Collections.emptyList();
        }
        var shops = shopService.getShopsV2(sortBy, shopsFilterCriterias, query);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/v3/shops")
    public ResponseEntity<ShopsResponseV3> getShopsV3(
        @RequestParam(name = "sorter", defaultValue = "NONE") ShopsSortCriteriaV3 sortBy,
        @RequestParam(name = "filter", required = false) List<ShopsFilterCriteriaV3> shopsFilterCriterias,
        @RequestParam(name = "query", required = false, defaultValue = "") String query
    ) {
        if (shopsFilterCriterias == null) {
            shopsFilterCriterias = Collections.emptyList();
        }
        var shops = shopService.getShopsV3(sortBy, shopsFilterCriterias, query);
        return ResponseEntity.ok(shops);
    }


    // 삭제 예정
    @GetMapping("/shops/search/related/{query}")
    public ResponseEntity<RelatedKeywordResponse> getRelatedKeyword(
            @PathVariable(name = "query") String query
    ) {
        return ResponseEntity.ok(shopSearchService.getRelatedKeywordByQuery(query));
    }

    @PostMapping("/shops/{shopId}/call-notification")
    public ResponseEntity<Void> createCallNotification(
            @Parameter(in = PATH) @PathVariable("shopId") Integer shopId,
            @Auth(permit = {GENERAL, STUDENT, COUNCIL}) Integer studentId
    ) {
        shopService.publishCallNotification(shopId, studentId);
        return ResponseEntity.ok().build();
    }
}
