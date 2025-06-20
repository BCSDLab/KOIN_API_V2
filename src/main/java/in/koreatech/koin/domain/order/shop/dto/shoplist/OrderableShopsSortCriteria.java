package in.koreatech.koin.domain.order.shop.dto.shoplist;

import java.util.Comparator;

import lombok.Getter;

@Getter
public enum OrderableShopsSortCriteria {

    NONE("NONE", Comparator.comparing(OrderableShopsResponse::name)),
    COUNT("COUNT", Comparator.comparingLong(OrderableShopsResponse::reviewCount).reversed()),
    COUNT_ASC("COUNT_ASCD", Comparator.comparingLong(OrderableShopsResponse::reviewCount)),
    COUNT_DESC("COUNT_DESC", Comparator.comparingLong(OrderableShopsResponse::reviewCount).reversed()),
    RATING("RATING", Comparator.comparingDouble(OrderableShopsResponse::ratingAverage).reversed()),
    RATING_ASC("RATING_ASCD", Comparator.comparingDouble(OrderableShopsResponse::ratingAverage)),
    RATING_DESC("RATING_DESC", Comparator.comparingDouble(OrderableShopsResponse::ratingAverage).reversed()),
    ;

    private final String value;
    private final Comparator<OrderableShopsResponse> comparator;

    OrderableShopsSortCriteria(String value, Comparator<OrderableShopsResponse> comparator) {
        this.value = value;
        this.comparator = comparator;
    }
}
