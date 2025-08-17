package in.koreatech.koin.domain.shop.dto.shop;

import java.util.List;
import java.util.function.Predicate;

import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV3.InnerShopResponse;
import lombok.Getter;

@Getter
public enum ShopsFilterCriteriaV3 {

    OPEN("OPEN"),
    DELIVERY("DELIVERY"),
    ;

    private final String value;

    ShopsFilterCriteriaV3(String value) {
        this.value = value;
    }

    public Predicate<InnerShopResponse> getCondition() {
        return switch (this) {
            case OPEN -> InnerShopResponse::isOpen;
            case DELIVERY -> InnerShopResponse::delivery;
            default -> shop -> true;
        };
    }

    public static Predicate<InnerShopResponse> createCombinedFilter(
        List<ShopsFilterCriteriaV3> criteriaList
    ) {
        return criteriaList.stream()
            .map(ShopsFilterCriteriaV3::getCondition)
            .reduce(x -> true, Predicate::and);
    }
}
