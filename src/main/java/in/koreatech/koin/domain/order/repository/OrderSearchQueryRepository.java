package in.koreatech.koin.domain.order.repository;

import static com.querydsl.core.types.dsl.Expressions.allOf;
import static in.koreatech.koin.domain.order.model.QOrder.order;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.payment.model.entity.QPayment.payment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.order.model.OrderInfo;
import in.koreatech.koin.domain.order.model.OrderSearchCriteria;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSearchQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Page<OrderInfo> findOrdersByCondition(Integer userId, OrderSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(criteria.page() - 1, criteria.limit());
        var predicate = allOf(
            order.user.id.eq(userId),
            criteria.period() != null ? criteria.period().getPredicate() : null,
            criteria.status() != null ? criteria.status().getPredicate() : null,
            criteria.type() != null ? criteria.type().getPredicate() : null
        );

        List<OrderInfo> results = jpaQueryFactory
            .select(Projections.constructor(OrderInfo.class,
                order.id,
                payment.id,
                order.orderableShop.id,
                order.orderableShopName,
                order.createdAt,
                order.status,
                payment.description))
            .from(order)
            .innerJoin(payment).on(payment.order.id.eq(order.id))
            .innerJoin(orderableShop).on(orderableShop.id.eq(order.orderableShop.id))
            .where(predicate)
            .orderBy(order.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = jpaQueryFactory
            .selectFrom(order)
            .innerJoin(payment).on(payment.order.id.eq(order.id))
            .innerJoin(orderableShop).on(orderableShop.id.eq(order.orderableShop.id))
            .where(predicate)
            .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }
}
