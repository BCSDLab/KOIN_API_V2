package in.koreatech.koin.domain.order.order.model;

import lombok.Getter;

@Getter
public enum OrderType {
    DELIVERY("배달"),
    TAKE_OUT("포장"),
    ;

    private final String name;

    OrderType(String name) {
        this.name = name;
    }
}
