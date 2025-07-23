package in.koreatech.koin.domain.order.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultSortCriteria;
import in.koreatech.koin.domain.order.shop.service.OrderableShopSearchService;
import in.koreatech.koin.domain.order.shop.usecase.OrderableShopSearchUseCase;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderableShopSearchController implements OrderableShopSearchApi {

    private final OrderableShopSearchUseCase orderableShopSearchUseCase;

    @GetMapping("/order/shop/search/{keyword}/related")
    public ResponseEntity<OrderableShopSearchRelatedKeywordResponse> searchRelatedKeyword(
        @PathVariable(name = "keyword") String keyword
    ) {
        return ResponseEntity.ok(orderableShopSearchUseCase.searchRelatedKeyword(keyword));
    }

    @GetMapping("/order/shop/search/{keyword}")
    public ResponseEntity<OrderableShopSearchResultResponse> searchOrderableShop(
        @RequestParam(name = "sorter", defaultValue = "NONE") OrderableShopSearchResultSortCriteria sortBy,
        @PathVariable(name = "keyword") String keyword
    ) {
        return ResponseEntity.ok(orderableShopSearchUseCase.searchByKeyword(keyword, sortBy));
    }
}
