package in.koreatech.koin.domain.shop.repository.order;

import static in.koreatech.koin.domain.shop.model.order.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.shop.model.order.QShopBaseDeliveryTip.shopBaseDeliveryTip;
import static in.koreatech.koin.domain.shop.model.order.QShopOperation.shopOperation;
import static in.koreatech.koin.domain.shop.model.review.QShopReview.shopReview;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;
import static in.koreatech.koin.domain.shop.model.shop.QShopCategoryMap.shopCategoryMap;
import static in.koreatech.koin.domain.shop.model.shop.QShopImage.shopImage;
import static in.koreatech.koin.domain.shop.model.shop.QShopOpen.shopOpen;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.dto.order.OrderableShopBaseInfo;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.order.OrderableShopsResponse.ShopOpenInfo;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderableShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    public List<OrderableShopBaseInfo> findAllOrderableShopInfo(
        List<OrderableShopsFilterCriteria> filterCriteria,
        Integer minimumAmount
    ) {
        var minimumTipSubquery = getMinimumDeliveryTipSubquery(); // 최소 배달비 서브 쿼리
        var maximumTipSubquery = getMaximumDeliveryTipSubquery(); // 최대 배달비 서브 쿼리
        var avgRatingExpression = getReviewRatingAvgExpression(); // 리뷰 평균 점수 반올림 표현식
        BooleanBuilder filter = orderableShopSearchFilter(filterCriteria, minimumAmount); // 동적 쿼리 필터

        return queryFactory
            .select(Projections.constructor(OrderableShopBaseInfo.class,
                shop.id,
                orderableShop.id,
                shop.name,
                orderableShop.delivery,
                orderableShop.takeout,
                orderableShop.minimumOrderAmount,
                avgRatingExpression,
                shopReview.id.count(),
                minimumTipSubquery,
                maximumTipSubquery,
                shopOperation.isOpen
            ))
            .from(shop)
            .innerJoin(orderableShop).on(orderableShop.shop.id.eq(shop.id))
            .innerJoin(shopOperation).on(shopOperation.shop.id.eq(orderableShop.shop.id))
            .leftJoin(shopReview).on(shopReview.shop.id.eq(shop.id)
                .and(shopReview.isDeleted.isFalse())
            )
            .where(filter)
            .groupBy(
                shop.id,
                shop.name,
                orderableShop.delivery,
                orderableShop.takeout,
                orderableShop.minimumOrderAmount
            )
            .fetch();
    }

    private BooleanBuilder orderableShopSearchFilter(List<OrderableShopsFilterCriteria> filterCriteria,
        Integer minimumAmount) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // 상점 최소 주문 금액 필터
        if (minimumAmount != null) {
            booleanBuilder.and(orderableShop.minimumOrderAmount.loe(minimumAmount));
        }

        // 영업 여부, 배달 가능, 포장 가능, 무료 배달 필터
        if (filterCriteria != null && !filterCriteria.isEmpty()) {
            for (OrderableShopsFilterCriteria criteria : filterCriteria) {
                BooleanExpression predicate = criteria.getPredicate();
                if (predicate != null) {
                    booleanBuilder.and(predicate);
                }
            }
        }

        return booleanBuilder;
    }

    private JPQLQuery<Integer> getMinimumDeliveryTipSubquery() {
        return JPAExpressions
            .select(shopBaseDeliveryTip.fee.min())
            .from(shopBaseDeliveryTip)
            .where(shopBaseDeliveryTip.shop.id.eq(shop.id));
    }

    private JPQLQuery<Integer> getMaximumDeliveryTipSubquery() {
        return JPAExpressions
            .select(shopBaseDeliveryTip.fee.max())
            .from(shopBaseDeliveryTip)
            .where(shopBaseDeliveryTip.shop.id.eq(shop.id));
    }

    private NumberExpression<Double> getReviewRatingAvgExpression() {
        return Expressions.numberTemplate(
            Double.class,
            "ROUND(COALESCE({0}, 0.0), 1)",
            shopReview.rating.avg()
        );
    }

    public Map<Integer, List<Integer>> findAllCategoriesByShopIds(List<Integer> shopIds) {
        return queryFactory
            .select(shopCategoryMap.shop.id, shopCategoryMap.shopCategory.id)
            .from(shopCategoryMap)
            .where(shopCategoryMap.shop.id.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(shopCategoryMap.shop.id),
                Collectors.mapping(tuple -> tuple.get(shopCategoryMap.shopCategory.id), Collectors.toList())
            ));
    }

    public Map<Integer, List<String>> findAllShopImagesByShopIds(List<Integer> shopIds) {
        return queryFactory
            .select(shopImage.shop.id, shopImage.imageUrl)
            .from(shopImage)
            .where(shopImage.shop.id.in(shopIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(shopImage.shop.id),
                Collectors.mapping(tuple -> tuple.get(shopImage.imageUrl), Collectors.toList())
            ));
    }

    public Map<Integer, List<ShopOpenInfo>> findAllShopOpensByShopIds(List<Integer> shopIds) {
        return queryFactory
            .select(shopOpen)
            .from(shopOpen)
            .where(shopOpen.shop.id.in(shopIds))
            .fetch()
            .stream()
            .map(opens -> new ShopOpenInfo(opens.getShop().getId(), opens.getDayOfWeek(), opens.isClosed(), opens.getOpenTime(), opens.getCloseTime()))
            .collect(Collectors.groupingBy(ShopOpenInfo::shopId));
    }
}
