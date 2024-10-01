package in.koreatech.koin.domain.shop.dto.shop;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import in.koreatech.koin.domain.shop.dto.shop.ShopsResponseV2.InnerShopResponse;
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
        switch (this) {
            case OPEN:
                return InnerShopResponse::isOpen;
            case DELIVERY:
                return InnerShopResponse::delivery;
            default:
                return shop -> true;
        }
    }

    public static Predicate<InnerShopResponse> createCombinedFilter(
        List<ShopsFilterCriteria> criteriaList
    ) {
        return criteriaList.stream()
            .map(criteria -> criteria.getCondition())
            .reduce(x -> true, Predicate::and);
    }
}
