package in.koreatech.koin.domain.shop.dto;

import java.util.Comparator;

import in.koreatech.koin.domain.shop.dto.ShopsResponseV2.InnerShopResponse;

public enum ShopsSortCriteria {

    NONE("NONE", (shop1, shop2) -> 0),
    COUNT("COUNT", Comparator.comparingLong(InnerShopResponse::reviewCount)),
    RATING("RATING", Comparator.comparingDouble(InnerShopResponse::averageRate))
    ;

    private final String value;
    private final Comparator<InnerShopResponse> comparator;

    ShopsSortCriteria(String value, Comparator<InnerShopResponse> comparator) {
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
