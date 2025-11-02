package in.koreatech.koin.domain.shop.dto.search.response;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.repository.shop.dto.ShopMenuNameKeywordHit;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopNameKeywordHit;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopSearchRelatedKeywordResponse(
    @Schema(description = "주변 상점 검색 입력 키워드", example = "치킨ㄱ")
    String searchKeyword,

    @Schema(description = "주변 상점 검색 처리 키워드", example = "치킨")
    String processedSearchKeyword,

    @Schema(description = "주변 상점 이름 관련 연관 키워드 개수", example = "5")
    Integer shopNameSearchResultCount,

    @Schema(description = "주변 상점 메뉴 이름 관련 연관 키워드 개수", example = "5")
    Integer menuNameSearchResultCount,

    List<InnerShopNameSearchRelatedKeywordResult> shopNameSearchResults,

    List<InnerMenuNameSearchRelatedKeywordResult> menuNameSearchResults
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopNameSearchRelatedKeywordResult(
        @Schema(description = "주변 상점 식별자", example = "5")
        Integer shopId,

        @Schema(description = "주변 상점 이름", example = "맛있는 치킨")
        String shopName
    ) {

        public static InnerShopNameSearchRelatedKeywordResult from(
            ShopNameKeywordHit shopNameKeywordHit
        ) {
            return new InnerShopNameSearchRelatedKeywordResult(
                shopNameKeywordHit.shopId(),
                shopNameKeywordHit.shopName()
            );
        }
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerMenuNameSearchRelatedKeywordResult(
        @Schema(description = "주변 상점 식별자", example = "5")
        Integer shopId,

        @Schema(description = "주변 상점 이름", example = "맛있는 치킨")
        String shopName,

        @Schema(description = "주변 상점 메뉴 이름", example = "간장 치킨 세트")
        String menuName
    ) {

        public static InnerMenuNameSearchRelatedKeywordResult from(
            ShopMenuNameKeywordHit shopMenuNameKeywordHit
        ) {
            return new InnerMenuNameSearchRelatedKeywordResult(
                shopMenuNameKeywordHit.shopId(),
                shopMenuNameKeywordHit.shopName(),
                shopMenuNameKeywordHit.menuName()
            );
        }
    }

    public static ShopSearchRelatedKeywordResponse from(
        String searchKeyword,
        List<String> processedSearchKeywords,
        List<ShopNameKeywordHit> shopNameSearchResult,
        List<ShopMenuNameKeywordHit> menuNameSearchResult
    ) {
        return new ShopSearchRelatedKeywordResponse(
            searchKeyword,
            String.join(" ", processedSearchKeywords),
            shopNameSearchResult.size(),
            menuNameSearchResult.size(),
            shopNameSearchResult.stream()
                .map(InnerShopNameSearchRelatedKeywordResult::from)
                .toList(),
            menuNameSearchResult.stream()
                .map(InnerMenuNameSearchRelatedKeywordResult::from)
                .toList()
        );
    }
}
