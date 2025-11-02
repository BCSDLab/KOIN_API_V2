package in.koreatech.koin.domain.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.shop.dto.search.response.ShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.shop.service.ShopSearchServiceV2;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShopSearchController implements ShopSearchApi {

    private final ShopSearchServiceV2 shopSearchServiceV2;

    @GetMapping("/v2/shops/search/related")
    public ResponseEntity<ShopSearchRelatedKeywordResponse> getRelatedKeyword(
        @RequestParam(name = "keyword") String keyword
    ) {
        return ResponseEntity.ok(shopSearchServiceV2.searchRelatedKeyword(keyword));
    }
}
