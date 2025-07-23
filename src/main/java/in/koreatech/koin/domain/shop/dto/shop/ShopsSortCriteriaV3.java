package in.koreatech.koin.domain.shop.dto.shop;

import java.util.Comparator;

import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV3.InnerShopResponse;

public enum ShopsSortCriteriaV3 {

    NONE("NONE", (shop1, shop2) -> 0),
    COUNT("COUNT", Comparator.comparingLong(InnerShopResponse::reviewCount).reversed()),
    COUNT_ASC("COUNT_ASCD", Comparator.comparingLong(InnerShopResponse::reviewCount)),
    COUNT_DESC("COUNT_DESC", Comparator.comparingLong(InnerShopResponse::reviewCount).reversed()),
    RATING("RATING", Comparator.comparingDouble(InnerShopResponse::averageRate).reversed()),
    RATING_ASC("RATING_ASCD", Comparator.comparingDouble(InnerShopResponse::averageRate)),
    RATING_DESC("RATING_DESC", Comparator.comparingDouble(InnerShopResponse::averageRate).reversed()),
    ;

    private final String value;
    private final Comparator<InnerShopResponse> comparator;

    ShopsSortCriteriaV3(String value, Comparator<InnerShopResponse> comparator) {
        this.value = value;
        this.comparator = comparator;
    }

    public String getValue() {
        return value;
    }

    public Comparator<InnerShopResponse> getComparator() {
        return comparator;
    }
}
