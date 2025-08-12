package in.koreatech.koin.domain.order.shop.dto.shopsearch;

import java.util.Comparator;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchResultResponse.OrderableShopSearchResult;
import lombok.Getter;

@Getter
public enum OrderableShopSearchResultSortCriteria {

    NONE("NONE", Comparator.comparing(OrderableShopSearchResult::name)),
    REVIEW_COUNT("REVIEW_COUNT", Comparator.comparingLong(OrderableShopSearchResult::reviewCount).reversed()),
    REVIEW_COUNT_ASC("REVIEW_COUNT_ASC", Comparator.comparingLong(OrderableShopSearchResult::reviewCount)),
    REVIEW_COUNT_DESC("REVIEW_COUNT_DESC", Comparator.comparingLong(OrderableShopSearchResult::reviewCount).reversed()),
    RATING("RATING", Comparator.comparingDouble(OrderableShopSearchResult::ratingAverage).reversed()),
    RATING_ASC("RATING_ASCD", Comparator.comparingDouble(OrderableShopSearchResult::ratingAverage)),
    RATING_DESC("RATING_DESC", Comparator.comparingDouble(OrderableShopSearchResult::ratingAverage).reversed()),
    ;

    private final String value;
    private final Comparator<OrderableShopSearchResult> comparator;

    OrderableShopSearchResultSortCriteria(String value, Comparator<OrderableShopSearchResult> comparator) {
        this.value = value;
        this.comparator = comparator;
    }
}
