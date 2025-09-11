package in.koreatech.koin.domain.order.order.dto.request;

import static in.koreatech.koin.domain.order.order.model.OrderStatus.*;
import static in.koreatech.koin.domain.order.order.model.QOrder.order;

import com.querydsl.core.types.dsl.BooleanExpression;

import in.koreatech.koin.domain.order.order.model.OrderStatus;
import lombok.Getter;

@Getter
public enum OrderStatusCriteria {

    NONE("NONE") {
        @Override
        public BooleanExpression getPredicate() {
            return order.status.in(DELIVERED, PICKED_UP, OrderStatus.CANCELED);
        }
    },
    COMPLETED("COMPLETED") {
        @Override
        public BooleanExpression getPredicate() {
            return order.status.in(DELIVERED, PICKED_UP);
        }
    },
    CANCELED("CANCELED") {
        @Override
        public BooleanExpression getPredicate() {
            return order.status.in(OrderStatus.CANCELED);
        }
    },
    ;

    private final String value;

    OrderStatusCriteria(String value) {
        this.value = value;
    }

    public abstract BooleanExpression getPredicate();
}
