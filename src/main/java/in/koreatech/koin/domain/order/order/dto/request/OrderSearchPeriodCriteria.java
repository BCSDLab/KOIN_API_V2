package in.koreatech.koin.domain.order.order.dto.request;

import static in.koreatech.koin.domain.order.model.QOrder.order;

import java.time.LocalDateTime;

import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.Getter;

@Getter
public enum OrderSearchPeriodCriteria {

    NONE("NONE") {
        @Override
        public BooleanExpression getPredicate() {
            return null;
        }
    },
    LAST_3_MONTHS("LAST_3_MONTHS") {
        @Override
        public BooleanExpression getPredicate() {
            return order.createdAt.goe(LocalDateTime.now().minusMonths(3));
        }
    },
    LAST_6_MONTHS("LAST_6_MONTHS") {
        @Override
        public BooleanExpression getPredicate() {
            return order.createdAt.goe(LocalDateTime.now().minusMonths(6));
        }
    },
    LAST_1_YEAR("LAST_1_YEAR") {
        @Override
        public BooleanExpression getPredicate() {
            return order.createdAt.goe(LocalDateTime.now().minusYears(1));
        }
    };

    private final String value;

    OrderSearchPeriodCriteria(String value) {
        this.value = value;
    }

    public abstract BooleanExpression getPredicate();
}
