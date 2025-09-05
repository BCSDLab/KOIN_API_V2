package in.koreatech.koin.domain.order.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    CONFIRMING("주문 확인중"),
    COOKING("조리 중"),
    PACKAGED("포장 완료"),
    DELIVERING("배달 중"),
    DELIVERED("배달 완료"),
    CANCELED("취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
