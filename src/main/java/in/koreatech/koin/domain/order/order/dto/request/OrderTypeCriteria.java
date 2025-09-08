package in.koreatech.koin.domain.order.order.dto.request;

import static in.koreatech.koin.domain.order.model.QOrder.order;

import com.querydsl.core.types.dsl.BooleanExpression;

import in.koreatech.koin.domain.order.order.model.OrderType;
import lombok.Getter;

@Getter
public enum OrderTypeCriteria {

    NONE("NONE") {
        @Override
        public BooleanExpression getPredicate() {
            return null;
        }
    },
    DELIVERY("DELIVERY") {
        @Override
        public BooleanExpression getPredicate() {
            return order.orderType.eq(OrderType.DELIVERY);
        }
    },
    TAKE_OUT("TAKE_OUT") {
        @Override
        public BooleanExpression getPredicate() {
            return order.orderType.eq(OrderType.TAKE_OUT);
        }
    },
    ;

    private final String value;

    OrderTypeCriteria(String value) {
        this.value = value;
    }

    public abstract BooleanExpression getPredicate();
}
