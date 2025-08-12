package in.koreatech.koin.domain.order.shop.repository;

import static in.koreatech.koin.domain.order.shop.model.entity.delivery.QShopBaseDeliveryTip.shopBaseDeliveryTip;
import static in.koreatech.koin.domain.order.shop.model.entity.menu.QOrderableShopMenu.orderableShopMenu;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShopImage.orderableShopImage;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QShopOperation.shopOperation;
import static in.koreatech.koin.domain.shop.model.review.QShopReview.shopReview;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.order.shop.model.readmodel.MenuNameKeywordHit;
import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse.InnerMenuNameSearchRelatedKeywordResult;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse.InnerShopNameSearchRelatedKeywordResult;
import in.koreatech.koin.domain.order.shop.model.readmodel.ShopNameKeywordHit;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderableShopSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ShopNameKeywordHit> findAllOrderableShopByKeyword(String keyword) {
        return queryFactory
            .select(Projections.constructor(ShopNameKeywordHit.class,
                orderableShop.id,
                shop.name
            ))
            .from(shop)
            .innerJoin(orderableShop).on(orderableShop.shop.id.eq(shop.id))
            .where(shop.name.contains(keyword))
            .fetch();
    }

    public List<MenuNameKeywordHit> findAllMenuByKeyword(String keyword) {
        return queryFactory
            .select(Projections.constructor(MenuNameKeywordHit.class,
                orderableShop.id,
                shop.name,
                orderableShopMenu.name
            ))
            .from(orderableShopMenu)
            .innerJoin(orderableShop).on(orderableShopMenu.orderableShop.eq(orderableShop))
            .innerJoin(shop).on(orderableShop.shop.eq(shop))
            .where(orderableShopMenu.name.contains(keyword))
            .fetch();
    }

    public List<OrderableShopBaseInfo> searchOrderableShopsByMenuKeyword(String keyword) {
        var avgRatingExpression = getReviewRatingAvgExpression();

        return queryFactory
            .select(Projections.constructor(OrderableShopBaseInfo.class,
                shop.id,
                orderableShop.id,
                shop.name,
                orderableShop.delivery,
                orderableShop.takeout,
                orderableShop.serviceEvent,
                orderableShop.minimumOrderAmount,
                avgRatingExpression,
                shopReview.id.count(),
                JPAExpressions.select(shopBaseDeliveryTip.fee.min())
                    .from(shopBaseDeliveryTip)
                    .where(shopBaseDeliveryTip.shop.id.eq(shop.id)),
                JPAExpressions.select(shopBaseDeliveryTip.fee.max())
                    .from(shopBaseDeliveryTip)
                    .where(shopBaseDeliveryTip.shop.id.eq(shop.id)),
                shopOperation.isOpen
            ))
            .from(orderableShopMenu)
            .innerJoin(orderableShopMenu.orderableShop, orderableShop)
            .innerJoin(orderableShop.shop, shop)
            .innerJoin(shopOperation).on(shopOperation.shop.id.eq(shop.id))
            .leftJoin(shopReview).on(shopReview.shop.id.eq(shop.id)
                .and(shopReview.isDeleted.isFalse())
            ).where(orderableShopMenu.name.contains(keyword))
            .groupBy(shop.id, orderableShop.id, shop.name,
                orderableShop.delivery, orderableShop.takeout,
                orderableShop.serviceEvent, orderableShop.minimumOrderAmount,
                shopOperation.isOpen)
            .fetch();
    }

    public List<OrderableShopBaseInfo> searchOrderableShopsByShopNameKeyword(String keyword) {
        var avgRatingExpression = getReviewRatingAvgExpression();

        return queryFactory
            .select(Projections.constructor(OrderableShopBaseInfo.class,
                shop.id,
                orderableShop.id,
                shop.name,
                orderableShop.delivery,
                orderableShop.takeout,
                orderableShop.serviceEvent,
                orderableShop.minimumOrderAmount,
                avgRatingExpression,
                shopReview.id.count(),
                JPAExpressions.select(shopBaseDeliveryTip.fee.min())
                    .from(shopBaseDeliveryTip)
                    .where(shopBaseDeliveryTip.shop.id.eq(shop.id)),
                JPAExpressions.select(shopBaseDeliveryTip.fee.max())
                    .from(shopBaseDeliveryTip)
                    .where(shopBaseDeliveryTip.shop.id.eq(shop.id)),
                shopOperation.isOpen
            ))
            .from(shop)
            .innerJoin(orderableShop).on(orderableShop.shop.id.eq(shop.id))
            .innerJoin(shopOperation).on(shopOperation.shop.id.eq(shop.id))
            .leftJoin(shopReview).on(shopReview.shop.id.eq(shop.id)
                .and(shopReview.isDeleted.isFalse())
            ).where(shop.name.contains(keyword))
            .groupBy(shop.id, orderableShop.id, shop.name,
                orderableShop.delivery, orderableShop.takeout,
                orderableShop.serviceEvent, orderableShop.minimumOrderAmount,
                shopOperation.isOpen)
            .fetch();
    }

    public Map<Integer, String> findOrderableShopThumbnailImageByOrderableShopIds(List<Integer> orderableShopIds) {
        return queryFactory
            .select(orderableShopImage.orderableShop.id, orderableShopImage.imageUrl)
            .from(orderableShopImage)
            .where(orderableShopImage.orderableShop.id.in(orderableShopIds)
                .and(orderableShopImage.isThumbnail.eq(true)))
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(orderableShopImage.orderableShop.id),
                tuple -> tuple.get(orderableShopImage.imageUrl),
                (existing, replacement) -> existing
            ));
    }

    public Map<Integer, List<String>> findOrderableShopContainMenuNameByOrderableShopIds(List<Integer> orderableShopIds, String keyword) {
        return queryFactory
            .select(orderableShopMenu.orderableShop.id, orderableShopMenu.name)
            .from(orderableShopMenu)
            .where(orderableShopMenu.orderableShop.id.in(orderableShopIds)
                .and(orderableShopMenu.name.contains(keyword))
            ).fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(orderableShopMenu.orderableShop.id),
                Collectors.mapping(tuple -> tuple.get(orderableShopMenu.name), Collectors.toList())
            ));
    }

    private NumberExpression<Double> getReviewRatingAvgExpression() {
        return Expressions.numberTemplate(
            Double.class,
            "ROUND(COALESCE({0}, 0.0), 1)",
            shopReview.rating.avg()
        );
    }
}
