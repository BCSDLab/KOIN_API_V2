package in.koreatech.koin.domain.order.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "(Normal) OrderableShopSearch: 주문 가능 상점 검색", description = "주문 가능 상점을 검색한다")
public interface OrderableShopSearchApi {

    @Operation(summary = "주문 가능 상점 검색 연관 키워드 조회", description = """
        ### 주문 가능 상점 검색 연관 키워드 조회
        - 주문 가능 상점 검색 시 연관 검색어 표시 화면에서 사용되는 API 입니다.
        - 상점 이름 중, 주어진 키워드와 일치하는 주문 가능 상점 이름, 식별자 목록을 반환합니다.
        - 메뉴 이름 중, 주어진 키워드와 일치하는 메뉴 이름과 해당 메뉴를 판매하는 주문 가능 상점 이름, 식별자 목록을 반환합니다.
        """)
    @GetMapping("/order/shop/search/{keyword}/related")
    ResponseEntity<OrderableShopSearchRelatedKeywordResponse> getSearchRelatedKeyword(
        @PathVariable(name = "keyword") String keyword
    );
}
