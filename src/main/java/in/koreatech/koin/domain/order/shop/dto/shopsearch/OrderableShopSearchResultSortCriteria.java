package in.koreatech.koin.domain.order.shop.dto.shopsearch;

import java.util.Comparator;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse.InnerOrderableShopSearchResult;
import lombok.Getter;

@Getter
public enum OrderableShopSearchResultSortCriteria {

    NONE("NONE", Comparator.comparing(InnerOrderableShopSearchResult::name)),
    REVIEW_COUNT("REVIEW_COUNT", Comparator.comparingLong(InnerOrderableShopSearchResult::reviewCount).reversed()),
    REVIEW_COUNT_ASC("REVIEW_COUNT_ASC", Comparator.comparingLong(InnerOrderableShopSearchResult::reviewCount)),
    REVIEW_COUNT_DESC("REVIEW_COUNT_DESC", Comparator.comparingLong(InnerOrderableShopSearchResult::reviewCount).reversed()),
    RATING("RATING", Comparator.comparingDouble(InnerOrderableShopSearchResult::ratingAverage).reversed()),
    RATING_ASC("RATING_ASCD", Comparator.comparingDouble(InnerOrderableShopSearchResult::ratingAverage)),
    RATING_DESC("RATING_DESC", Comparator.comparingDouble(InnerOrderableShopSearchResult::ratingAverage).reversed()),
    ;

    private final String value;
    private final Comparator<InnerOrderableShopSearchResult> comparator;

    OrderableShopSearchResultSortCriteria(String value, Comparator<InnerOrderableShopSearchResult> comparator) {
        this.value = value;
        this.comparator = comparator;
    }
}
