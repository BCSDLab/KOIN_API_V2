package in.koreatech.koin.domain.shop.dto.order;

import static in.koreatech.koin.domain.shop.model.order.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.shop.model.order.QShopBaseDeliveryTip.shopBaseDeliveryTip;
import static in.koreatech.koin.domain.shop.model.order.QShopOperation.shopOperation;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

import lombok.Getter;

@Getter
public enum OrderableShopsFilterCriteria {

    IS_OPEN("IS_OPEN", true) {
        @Override
        public BooleanExpression getPredicate() {
            return shopOperation.isOpen.eq(this.getValue());
        }
    },
    DELIVERY_AVAILABLE("DELIVERY_AVAILABLE", true) {
        @Override
        public BooleanExpression getPredicate() {
            return orderableShop.delivery.eq(this.getValue());
        }
    },
    TAKEOUT_AVAILABLE("TAKEOUT_AVAILABLE", true) {
        @Override
        public BooleanExpression getPredicate() {
            return orderableShop.takeout.eq(this.getValue());
        }
    },
    FREE_DELIVERY_TIP("FREE_DELIVERY_TIP", true) {
        @Override
        public BooleanExpression getPredicate() {
            return JPAExpressions
                .select(shopBaseDeliveryTip.fee.max())
                .from(shopBaseDeliveryTip)
                .where(shopBaseDeliveryTip.shop.id.eq(shop.id)) // shop.id는 현재 쿼리의 메인 shop 엔티티를 참조해야 함
                .eq(0);
        }
    }
    ;

    private final String name;
    private final Boolean value;

    OrderableShopsFilterCriteria(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public abstract BooleanExpression getPredicate();
}
