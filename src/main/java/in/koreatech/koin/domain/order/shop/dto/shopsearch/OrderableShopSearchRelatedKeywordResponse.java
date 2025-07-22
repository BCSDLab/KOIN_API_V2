package in.koreatech.koin.domain.order.shop.dto.shopsearch;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopSearchRelatedKeywordResponse(
    @Schema(description = "주문 가능 상점 이름 관련 연관 키워드 개수", example = "5")
    Integer shopNameSearchResultCount,
    @Schema(description = "주문 가능 상점 메뉴 이름 관련 연관 키워드 개수", example = "5")
    Integer MenuNameSearchResultCount,
    List<InnerShopNameSearchRelatedKeywordResult> shopNameSearchResults,
    List<InnerMenuNameSearchRelatedKeywordResult> menuNameSearchResults
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopNameSearchRelatedKeywordResult(
        @Schema(description = "주문 가능 상점 식별자", example = "5")
        Integer orderableShopId,
        @Schema(description = "주문 가능 상점 이름", example = "맛있는 치킨")
        String orderableShopName
    ) {

    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerMenuNameSearchRelatedKeywordResult(
        @Schema(description = "주문 가능 상점 식별자", example = "5")
        Integer orderableShopId,
        @Schema(description = "주문 가능 상점 이름", example = "맛있는 치킨")
        String orderableShopName,
        @Schema(description = "주문 가능 상점 메뉴 이름", example = "간장 치킨 세트")
        String menuName
    ) {

    }

    public static OrderableShopSearchRelatedKeywordResponse from(
        List<InnerShopNameSearchRelatedKeywordResult> shopNameSearchResult,
        List<InnerMenuNameSearchRelatedKeywordResult> menuNameSearchResult
    ) {
        return new OrderableShopSearchRelatedKeywordResponse(
            shopNameSearchResult.size(),
            menuNameSearchResult.size(),
            shopNameSearchResult,
            menuNameSearchResult
        );
    }
}
