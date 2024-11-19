package in.koreatech.koin.domain.shop.dto.shop;

import java.util.List;
import java.util.function.Predicate;

import in.koreatech.koin.domain.shop.dto.shop.response.ShopsResponseV2.InnerShopResponse;
import lombok.Getter;

@Getter
public enum ShopsFilterCriteria {

    OPEN("OPEN"),
    DELIVERY("DELIVERY"),
    ;

    private final String value;

    ShopsFilterCriteria(String value) {
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
        List<ShopsFilterCriteria> criteriaList
    ) {
        return criteriaList.stream()
            .map(ShopsFilterCriteria::getCondition)
            .reduce(x -> true, Predicate::and);
    }
}
