package in.koreatech.koin.domain.order.shop.repository;

import static in.koreatech.koin.domain.order.shop.model.entity.delivery.QShopBaseDeliveryTip.shopBaseDeliveryTip;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShopImage.orderableShopImage;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QShopOperation.shopOperation;
import static in.koreatech.koin.domain.shop.model.review.QShopReview.shopReview;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;
import static in.koreatech.koin.domain.shop.model.shop.QShopCategory.shopCategory;
import static in.koreatech.koin.domain.shop.model.shop.QShopCategoryMap.shopCategoryMap;
import static in.koreatech.koin.domain.shop.model.shop.QShopOpen.shopOpen;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopBaseInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopCategoryFilterCriteria;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopImageInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopOpenInfo;
import in.koreatech.koin.domain.order.shop.dto.shoplist.OrderableShopsFilterCriteria;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderableShopListQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<OrderableShopBaseInfo> findAllOrderableShopInfo(
        List<OrderableShopsFilterCriteria> filterCriteria,
        OrderableShopCategoryFilterCriteria orderableShopCategoryFilterCriteria,
        Integer minimumAmount
    ) {
        var minimumTipSubquery = getMinimumDeliveryTipSubquery(); // 최소 배달비 서브 쿼리
        var maximumTipSubquery = getMaximumDeliveryTipSubquery(); // 최대 배달비 서브 쿼리
        var averageReviewRatingSubquery = getAvgReviewRatingSubquery(); // 리뷰 평균 별점 서브 쿼리
        var reviewCountSubquery = getReviewCountSubquery(); // 리뷰 개수 서브 쿼리
        BooleanBuilder filter = orderableShopSearchFilter(filterCriteria, minimumAmount); // 동적 쿼리 필터

        JPAQuery<OrderableShopBaseInfo> baseQuery = queryFactory
            .select(Projections.constructor(OrderableShopBaseInfo.class,
                shop.id,
                orderableShop.id,
                shop.name,
                orderableShop.delivery,
                orderableShop.takeout,
                orderableShop.serviceEvent,
                orderableShop.minimumOrderAmount,
                averageReviewRatingSubquery,
                reviewCountSubquery,
                minimumTipSubquery,
                maximumTipSubquery,
                shopOperation.isOpen
            ))
            .from(shop)
            .innerJoin(orderableShop).on(orderableShop.shop.id.eq(shop.id))
            .innerJoin(shopOperation).on(shopOperation.shop.id.eq(orderableShop.shop.id));

        // 카테고리 필터 조건이 있는 경우에만 shop_categories 동적 으로 조인
        if (!orderableShopCategoryFilterCriteria.equals(OrderableShopCategoryFilterCriteria.ALL)) {
            baseQuery = baseQuery
                .innerJoin(shopCategoryMap).on(shopCategoryMap.shop.id.eq(shop.id))
                .innerJoin(shopCategory).on(shopCategory.id.eq(shopCategoryMap.shopCategory.id));

            filter.and(shopCategory.id.eq(orderableShopCategoryFilterCriteria.getValue()));
        }
        return baseQuery
            .where(filter)
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

    private NumberTemplate<Double> getAvgReviewRatingSubquery() {
        return Expressions.numberTemplate(
            Double.class,
            "ROUND(COALESCE({0}, 0.0), 1)",
            JPAExpressions
                .select(shopReview.rating.avg().coalesce(0.0))
                .from(shopReview)
                .where(shopReview.shop.id.eq(shop.id)
                    .and(shopReview.isDeleted.isFalse()))
        );
    }

    private JPQLQuery<Long> getReviewCountSubquery() {
        return JPAExpressions
            .select(shopReview.id.count())
            .from(shopReview)
            .where(shopReview.shop.id.eq(shop.id)
                .and(shopReview.isDeleted.isFalse()));
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

    public Map<Integer, List<OrderableShopOpenInfo>> findAllShopOpensByShopIds(List<Integer> shopIds) {
        return queryFactory
            .select(shopOpen)
            .from(shopOpen)
            .where(shopOpen.shop.id.in(shopIds))
            .fetch()
            .stream()
            .map(opens -> new OrderableShopOpenInfo(opens.getShop().getId(), opens.getDayOfWeek(), opens.isClosed(),
                opens.getOpenTime(), opens.getCloseTime()))
            .collect(Collectors.groupingBy(OrderableShopOpenInfo::shopId));
    }

    public Map<Integer, List<OrderableShopImageInfo>> findAllOrderableShopImagesByOrderableShopIds(List<Integer> orderableShopIds) {
        return queryFactory
            .select(orderableShopImage)
            .from(orderableShopImage)
            .where(orderableShopImage.orderableShop.id.in(orderableShopIds))
            .fetch()
            .stream()
            .map(image -> new OrderableShopImageInfo(image.getOrderableShop().getId(), image.getImageUrl(), image.getIsThumbnail()))
            .collect(Collectors.groupingBy(OrderableShopImageInfo::orderableShopId));
    }
}
