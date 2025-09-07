package in.koreatech.koin.domain.order.order.dto.request;

import static in.koreatech.koin.domain.order.model.OrderStatus.DELIVERED;
import static in.koreatech.koin.domain.order.model.OrderStatus.PACKAGED;
import static in.koreatech.koin.domain.order.model.QOrder.order;

import com.querydsl.core.types.dsl.BooleanExpression;

import in.koreatech.koin.domain.order.model.OrderStatus;
import lombok.Getter;

@Getter
public enum OrderStatusCriteria {

    NONE("NONE") {
        @Override
        public BooleanExpression getPredicate() {
            return null;
        }
    },
    COMPLETED("COMPLETED") {
        @Override
        public BooleanExpression getPredicate() {
            return order.status.in(DELIVERED, PACKAGED);
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
