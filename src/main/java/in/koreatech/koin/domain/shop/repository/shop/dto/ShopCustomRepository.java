package in.koreatech.koin.domain.shop.repository.shop.dto;

import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;
import static in.koreatech.koin.domain.shop.model.shop.QShopImage.shopImage;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.model.event.QEventArticle;
import in.koreatech.koin.domain.shop.model.review.QShopReview;
import in.koreatech.koin.domain.shop.model.shop.QShop;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Integer, ShopInfo> findAllShopInfo(LocalDateTime now) {
        QShop shop = QShop.shop;
        QEventArticle eventArticle = QEventArticle.eventArticle;
        QShopReview shopReview = QShopReview.shopReview;

        Map<Integer, Boolean> eventMap = queryFactory
            .select(shop.id, eventArticle.id.count().gt(0L))
            .from(shop)
            .leftJoin(shop.eventArticles, eventArticle)
            .on(eventArticle.startDate.loe(now.toLocalDate())
                .and(eventArticle.endDate.goe(now.toLocalDate())))
            .groupBy(shop.id)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(0, Integer.class),
                tuple -> tuple.get(1, Boolean.class)
            ));

        List<Tuple> reviewResults = queryFactory
            .select(
                shop.id,
                Expressions.numberTemplate(Double.class,
                    "ROUND(COALESCE({0}, 0), 1)", shopReview.rating.avg()),
                shopReview.id.count()
            )
            .from(shop)
            .leftJoin(shop.reviews, shopReview)
            .on(shopReview.isDeleted.eq(false))
            .groupBy(shop.id)
            .fetch();

        Map<Integer, ShopInfo> map = new HashMap<>(reviewResults.size());
        for (Tuple result : reviewResults) {
            Integer shopId = result.get(0, Integer.class);
            ShopInfo shopInfo = new ShopInfo(
                eventMap.getOrDefault(shopId, false),
                result.get(1, Double.class),
                result.get(2, Long.class)
            );
            map.put(shopId, shopInfo);
        }

        return map;
    }

    public Map<Integer, List<String>> findAllShopImage() {
        return queryFactory
            .select(shop.id, shopImage.imageUrl)
            .from(shopImage)
            .innerJoin(shopImage.shop, shop)
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(shop.id),
                Collectors.mapping(tuple -> tuple.get(shopImage.imageUrl), Collectors.toList())
            ));
    }

    public List<Integer> findAllOrderableShopId() {
        return queryFactory
            .select(orderableShop.shop.id)
            .from(orderableShop)
            .fetch();
    }
}
