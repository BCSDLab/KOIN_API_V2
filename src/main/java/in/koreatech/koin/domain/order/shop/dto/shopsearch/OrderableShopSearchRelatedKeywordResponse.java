package in.koreatech.koin.domain.order.shop.dto.shopsearch;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.readmodel.MenuNameKeywordHit;
import in.koreatech.koin.domain.order.shop.model.readmodel.ShopNameKeywordHit;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopSearchRelatedKeywordResponse(
    @Schema(description = "주문 가능 상점 검색 입력 키워드", example = "치킨ㄱ")
    String searchKeyword,

    @Schema(description = "주문 가능 상점 검색 처리 키워드", example = "치킨")
    String processedSearchKeyword,

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

        public static InnerShopNameSearchRelatedKeywordResult from(ShopNameKeywordHit shopNameKeywordHit) {
            return new InnerShopNameSearchRelatedKeywordResult(
                shopNameKeywordHit.orderableShopId(),
                shopNameKeywordHit.orderableShopName()
            );
        }
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

        public static InnerMenuNameSearchRelatedKeywordResult from(MenuNameKeywordHit menuNameKeywordHit) {
            return new InnerMenuNameSearchRelatedKeywordResult(
                menuNameKeywordHit.orderableShopId(),
                menuNameKeywordHit.orderableShopName(),
                menuNameKeywordHit.menuName()
            );
        }
    }

    public static OrderableShopSearchRelatedKeywordResponse from(
        String searchKeyword,
        String processedSearchKeyword,
        List<ShopNameKeywordHit> shopNameSearchResult,
        List<MenuNameKeywordHit> menuNameSearchResult
    ) {
        return new OrderableShopSearchRelatedKeywordResponse(
            searchKeyword,
            processedSearchKeyword,
            shopNameSearchResult.size(),
            menuNameSearchResult.size(),
            shopNameSearchResult.stream().map(InnerShopNameSearchRelatedKeywordResult::from).toList(),
            menuNameSearchResult.stream().map(InnerMenuNameSearchRelatedKeywordResult::from).toList()
        );
    }
}
