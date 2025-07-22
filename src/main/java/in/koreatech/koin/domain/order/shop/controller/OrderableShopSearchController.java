package in.koreatech.koin.domain.order.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.order.shop.service.OrderableShopSearchService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderableShopSearchController implements OrderableShopSearchApi {

    private final OrderableShopSearchService orderableShopSearchService;

    @GetMapping("/order/shop/search/{keyword}/related")
    public ResponseEntity<OrderableShopSearchRelatedKeywordResponse> getSearchRelatedKeyword(
        @PathVariable(name = "keyword") String keyword
    ) {
        return ResponseEntity.ok(orderableShopSearchService.searchRelatedKeyword(keyword));
    }
}
