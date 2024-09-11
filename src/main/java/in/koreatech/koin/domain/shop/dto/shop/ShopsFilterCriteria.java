package in.koreatech.koin.domain.shop.dto.shop;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import in.koreatech.koin.domain.shop.model.shop.Shop;
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

    public Predicate<Shop> getCondition(LocalDateTime now) {
        switch (this) {
            case OPEN:
                return shop -> shop.isOpen(now);
            case DELIVERY:
                return Shop::getDelivery;
            default:
                return shop -> true;
        }
    }

    public static Predicate<Shop> createCombinedFilter(List<ShopsFilterCriteria> criteriaList, LocalDateTime now) {
        return criteriaList.stream()
            .map(criteria -> criteria.getCondition(now))
            .reduce(x -> true, Predicate::and);
    }
}
