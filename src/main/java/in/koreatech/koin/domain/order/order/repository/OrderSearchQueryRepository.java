package in.koreatech.koin.domain.order.order.repository;

import static com.querydsl.core.types.dsl.Expressions.allOf;
import static com.querydsl.core.types.dsl.Expressions.anyOf;
import static in.koreatech.koin.domain.order.order.model.QOrder.order;
import static in.koreatech.koin.domain.order.order.model.QOrderMenu.orderMenu;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShopImage.orderableShopImage;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QShopOperation.shopOperation;

import static in.koreatech.koin.domain.payment.model.entity.QPayment.payment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.order.order.model.OrderInfo;
import in.koreatech.koin.domain.order.order.model.OrderSearchCriteria;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderSearchQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Page<OrderInfo> findOrdersByCondition(Integer userId, OrderSearchCriteria criteria) {
        Pageable pageable = PageRequest.of(criteria.page() - 1, criteria.limit());
        var normalized = normalize(criteria.query());

        var predicate = allOf(
            order.user.id.eq(userId),
            criteria.period() != null ? criteria.period().getPredicate() : null,
            criteria.status() != null ? criteria.status().getPredicate() : null,
            criteria.type() != null ? criteria.type().getPredicate() : null,
            anyOf(
                orderableShop.shop.internalName.contains(normalized),
                JPAExpressions.selectOne()
                    .from(orderMenu)
                    .where(
                        orderMenu.order.id.eq(order.id)
                            .and(orderMenu.menuName.contains(normalized))
                    )
                    .exists()
            )
        );

        List<OrderInfo> results = jpaQueryFactory
            .select(Projections.constructor(OrderInfo.class,
                order.id,
                payment.id,
                order.orderableShop.id,
                order.orderableShopName,
                shopOperation.isOpen,
                getOrderableShopThumbnailSubquery(),
                order.createdAt,
                order.status,
                payment.description,
                order.totalPrice))
            .from(order)
            .innerJoin(payment).on(payment.order.id.eq(order.id))
            .innerJoin(orderableShop).on(orderableShop.id.eq(order.orderableShop.id))
            .innerJoin(shopOperation).on(shopOperation.shop.id.eq(orderableShop.shop.id))
            .where(predicate)
            .orderBy(order.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = jpaQueryFactory
            .selectFrom(order)
            .innerJoin(payment).on(payment.order.id.eq(order.id))
            .innerJoin(orderableShop).on(orderableShop.id.eq(order.orderableShop.id))
            .innerJoin(orderMenu).on(orderMenu.order.id.eq(order.id))
            .where(predicate)
            .fetchCount();

        return new PageImpl<>(results, pageable, total);
    }

    private JPQLQuery<String> getOrderableShopThumbnailSubquery() {
        return JPAExpressions
            .select(orderableShopImage.imageUrl)
            .from(orderableShopImage)
            .where(orderableShopImage.orderableShop.id.eq(orderableShop.id)
                .and(orderableShopImage.isThumbnail.eq(true)));
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }
}
