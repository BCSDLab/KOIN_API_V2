package in.koreatech.koin.domain.shop.repository.shop.dto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.model.article.QEventArticle;
import in.koreatech.koin.domain.shop.model.review.QShopReview;
import in.koreatech.koin.domain.shop.model.shop.QShop;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Integer, ShopInfo> findAllShopInfo(LocalDate now) {
        QShop shop = QShop.shop;
        QEventArticle eventArticle = QEventArticle.eventArticle;
        QShopReview shopReview = QShopReview.shopReview;

        List<Tuple> results = queryFactory
            .select(
                shop.id,
                ExpressionUtils.as(eventArticle.id.count().gt(0L), "isEventActive"),
                ExpressionUtils.as(
                    Expressions.numberTemplate(Double.class, "ROUND(COALESCE({0}, 0), 1)", shopReview.rating.avg()),
                    "averageRate"
                ),
                shopReview.id.count().as("reviewCount")
            )
            .from(shop)
            .leftJoin(shop.eventArticles, eventArticle)
            .on(eventArticle.startDate.loe(now)
                .and(eventArticle.endDate.goe(now)))
            .leftJoin(shop.reviews, shopReview)
            .groupBy(shop.id)
            .fetch();

        Map<Integer, ShopInfo> map = new HashMap<>(results.size());

        for (Tuple result : results) {
            ShopInfo shopResult = new ShopInfo(
                result.get(1, Boolean.class),
                result.get(2, Double.class),
                result.get(3, Long.class)
            );
            map.put(result.get(0, Integer.class), shopResult);
        }
        return map;
    }

    public Map<Integer, Boolean> findAllShopEvent(LocalDate now) {
        QShop shop = QShop.shop;
        QEventArticle eventArticle = QEventArticle.eventArticle;

        List<Tuple> results = queryFactory
            .select(
                shop.id,
                ExpressionUtils.as(eventArticle.id.count().gt(0L), "isEventActive")
            )
            .from(shop)
            .leftJoin(shop.eventArticles, eventArticle)
            .on(eventArticle.startDate.loe(now).and(eventArticle.endDate.goe(now)))
            .groupBy(shop.id)
            .fetch();

        Map<Integer, Boolean> shopEventMap = new HashMap<>(results.size());

        for (Tuple result : results) {
            shopEventMap.put(
                result.get(0, Integer.class),
                result.get(1, Boolean.class)
            );
        }
        return shopEventMap;
    }
}
