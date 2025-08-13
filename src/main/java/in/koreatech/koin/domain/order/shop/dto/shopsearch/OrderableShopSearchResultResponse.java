package in.koreatech.koin.domain.order.shop.dto.shopsearch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopOpenStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopSearchResultResponse(
    @Schema(description = "주문 가능 상점 검색 입력 키워드", example = "치킨ㄱ")
    String searchKeyword,

    @Schema(description = "주문 가능 상점 검색 처리 키워드", example = "치킨")
    String processedSearchKeyword,

    @Schema(description = "주문 가능 상점 검색 결과 개수", example = "5")
    Integer resultCount,

    @Schema(description = "주문 가능 상점 검색 결과")
    List<OrderableShopSearchResult> searchResults
) {

    public static OrderableShopSearchResultResponse empty(String searchKeyword, List<String> processedSearchKeywords) {
        return new OrderableShopSearchResultResponse(
            searchKeyword,
            String.join(" ", processedSearchKeywords),
            0,
            Collections.emptyList()
        );
    }

    public static OrderableShopSearchResultResponse from(
        String searchKeyword,
        List<String> processedSearchKeywords,
        List<OrderableShopSearchResult> searchResults
    ) {
        return new OrderableShopSearchResultResponse(
            searchKeyword,
            String.join(" ", processedSearchKeywords),
            searchResults.size(),
            searchResults
        );
    }
}
