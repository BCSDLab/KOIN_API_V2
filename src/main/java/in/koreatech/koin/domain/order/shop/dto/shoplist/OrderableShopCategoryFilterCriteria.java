package in.koreatech.koin.domain.order.shop.dto.shoplist;

import java.util.List;

import lombok.Getter;

@Getter
public enum OrderableShopCategoryFilterCriteria {

    ALL("ALL", null),
    CHICKEN("CHICKEN", 2),
    PIZZA_BUGGER("PIZZA_BUGGER", 3),
    DOSIRAK_BUNSIK("DOSIRAK_BUNSIK", 4),
    JOKBAL("JOKBAL", 5),
    CHINESE_RESTAURANT("CHINESE_RESTAURANT", 6),
    MEAT_RESTAURANT("MEAT_RESTAURANT", 7),
    KOREAN_RESTAURANT("KOREAN_RESTAURANT", 8),
    PUB("PUB", 9),
    CAFE("CAFE", 10),
    CALL_VAN("CALL_VAN", 11),
    ETC("ETC", 12)
    ;

    private final String name;
    private final Integer value;

    OrderableShopCategoryFilterCriteria(String name, Integer value) {
        this.name = name;
        this.value = value;
    }
}
