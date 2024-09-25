package in.koreatech.koin.domain.shop.repository.shop.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.model.article.QEventArticle;
import in.koreatech.koin.domain.shop.model.review.QShopReview;
import in.koreatech.koin.domain.shop.model.shop.QShop;
import in.koreatech.koin.domain.shop.model.shop.QShopOpen;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Integer, ShopInfoV2> findAllShopInfo(LocalDateTime now) {
        QShop shop = QShop.shop;
        QEventArticle eventArticle = QEventArticle.eventArticle;
        QShopReview shopReview = QShopReview.shopReview;
        QShopOpen shopOpen = QShopOpen.shopOpen;

        BooleanExpression isOpenSubQuery = JPAExpressions
            .selectOne()
            .from(shopOpen)
            .where(shopOpen.shop.id.eq(shop.id)
                .and(openCondition(shopOpen.shop.id, now)))
            .exists();

        List<Tuple> results = queryFactory
            .select(
                shop.id,
                ExpressionUtils.as(eventArticle.id.count().gt(0L), "isEventActive"),
                ExpressionUtils.as(
                    Expressions.numberTemplate(Double.class, "ROUND(COALESCE({0}, 0), 1)", shopReview.rating.avg()),
                    "averageRate"
                ),
                shopReview.id.count().as("reviewCount"),
                ExpressionUtils.as(isOpenSubQuery, "isOpen") // 서브쿼리 결과를 "isOpen" 필드로 추가
            )
            .from(shop)
            .leftJoin(shop.eventArticles, eventArticle)
            .on(eventArticle.startDate.loe(now.toLocalDate())
                .and(eventArticle.endDate.goe(now.toLocalDate())))
            .leftJoin(shop.reviews, shopReview)
            .groupBy(shop.id)
            .fetch();

        Map<Integer, ShopInfoV2> map = new HashMap<>(results.size());

        for (Tuple result : results) {
            ShopInfoV2 shopResult = new ShopInfoV2(
                result.get(1, Boolean.class),
                result.get(2, Double.class),
                result.get(3, Long.class),
                result.get(4, Boolean.class)
            );
            map.put(result.get(0, Integer.class), shopResult);
        }
        return map;
    }

    public Map<Integer, ShopInfoV1> findAllShopEvent(LocalDateTime now) {
        QShop shop = QShop.shop;
        QEventArticle eventArticle = QEventArticle.eventArticle;
        QShopOpen shopOpen = QShopOpen.shopOpen;

        BooleanExpression isOpenSubQuery = JPAExpressions
            .selectOne()
            .from(shopOpen)
            .where(shopOpen.shop.id.eq(shop.id)
                .and(openCondition(shopOpen.shop.id, now)))
            .exists();

        List<Tuple> results = queryFactory
            .select(
                shop.id,
                ExpressionUtils.as(eventArticle.id.count().gt(0L), "isEventActive"),
                ExpressionUtils.as(isOpenSubQuery, "isOpen") // 서브쿼리 결과를 "isOpen" 필드로 추가
            )
            .from(shop)
            .leftJoin(shop.eventArticles, eventArticle)
            .on(eventArticle.startDate.loe(now.toLocalDate()).and(eventArticle.endDate.goe(
                LocalDate.from(now.toLocalDate()))))
            .groupBy(shop.id)
            .fetch();

        Map<Integer, ShopInfoV1> shopEventMap = new HashMap<>(results.size());

        for (Tuple result : results) {
            ShopInfoV1 shopResult = new ShopInfoV1(
                result.get(1, Boolean.class),
                result.get(2, Boolean.class)
            );
            shopEventMap.put(result.get(0, Integer.class), shopResult);
        }
        return shopEventMap;
    }

    private BooleanBuilder openCondition(NumberPath<Integer> shopId, LocalDateTime now) {
        QShopOpen shopOpen = QShopOpen.shopOpen; // 상점 운영 시간 테이블
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDate yesterdayDate = yesterday.toLocalDate();

        BooleanBuilder whereCondition = new BooleanBuilder();

        // 조건 1: 오늘의 요일과 시간이 open_time과 close_time 사이에 있는지 확인
        whereCondition.or(
            shopOpen.dayOfWeek.eq(now.getDayOfWeek().name()) // 오늘의 요일을 문자열로 비교
                .and(shopOpen.openTime.loe(currentTime)) // openTime <= 현재 시간
                .and(shopOpen.closeTime.goe(currentTime) // closeTime >= 현재 시간
                    .or(shopOpen.openTime.goe(shopOpen.closeTime) // openTime >= closeTime (영업 시간이 다음날로 넘어가는 경우)
                        .and(shopOpen.closeTime.goe(currentTime)))) // closeTime >= 현재 시간
        );

        // 조건 2: 어제의 요일과 시간이 open_time과 close_time 사이에 있는지 확인
        whereCondition.or(
            shopOpen.dayOfWeek.eq(yesterday.getDayOfWeek().name()) // 어제의 요일을 문자열로 비교
                .and(shopOpen.openTime.loe(currentTime)) // openTime <= 현재 시간
                .and(shopOpen.closeTime.goe(currentTime) // closeTime >= 현재 시간
                    .or(shopOpen.openTime.goe(shopOpen.closeTime) // openTime >= closeTime (영업 시간이 다음날로 넘어가는 경우)
                        .and(shopOpen.closeTime.goe(currentTime)))) // closeTime >= 현재 시간
        );

        // shop_id가 특정 값인 조건
        return whereCondition.and(shopOpen.shop.id.eq(shopId));
    }
}
