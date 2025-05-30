package in.koreatech.koin.domain.shop.repository.shop.dto;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import in.koreatech.koin.domain.shop.model.event.QEventArticle;
import in.koreatech.koin.domain.shop.model.review.QShopReview;
import in.koreatech.koin.domain.shop.model.shop.QShop;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Integer, ShopInfo> findAllShopInfo(LocalDateTime now) {
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
            .on(eventArticle.startDate.loe(now.toLocalDate())
                .and(eventArticle.endDate.goe(now.toLocalDate())))
            .leftJoin(shop.reviews, shopReview)
            .on(shopReview.isDeleted.eq(false))
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
}
