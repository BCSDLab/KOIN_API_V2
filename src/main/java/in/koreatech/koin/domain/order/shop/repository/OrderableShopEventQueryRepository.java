package in.koreatech.koin.domain.order.shop.repository;

import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.shop.model.event.QEventArticle.eventArticle;
import static in.koreatech.koin.domain.shop.model.event.QEventArticleImage.eventArticleImage;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.order.shop.model.readmodel.OrderableShopEvent;
import in.koreatech.koin.domain.shop.model.event.EventArticleImage;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderableShopEventQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<OrderableShopEvent> getAllOngoingEvents() {
        LocalDate today = LocalDate.now();

        return jpaQueryFactory.select(Projections.constructor(OrderableShopEvent.class,
                orderableShop.id,
                orderableShop.shop.id,
                orderableShop.shop.name,
                eventArticle.id,
                eventArticle.title,
                eventArticle.content,
                eventArticle.startDate,
                eventArticle.endDate)
            ).from(orderableShop)
            .innerJoin(orderableShop.shop, shop)
            .innerJoin(shop.eventArticles, eventArticle)
            .where(
                orderableShop.isDeleted.isFalse(),
                shop.isDeleted.isFalse(),
                eventArticle.isDeleted.isFalse(),
                eventArticleIsOngoing(today)
            ).fetch();
    }

    public List<OrderableShopEvent> getOngoingEventById(Integer orderableShopId) {
        LocalDate today = LocalDate.now();
        boolean orderableShopExists = jpaQueryFactory.select(orderableShop.id)
            .from(orderableShop)
            .innerJoin(orderableShop.shop, shop)
            .where(
                orderableShop.id.eq(orderableShopId),
                orderableShop.isDeleted.isFalse(),
                shop.isDeleted.isFalse()
            )
            .fetchFirst() != null;

        if (!orderableShopExists) {
            throw CustomException.of(ApiResponseCode.NOT_FOUND_ORDERABLE_SHOP,
                "해당 상점이 존재하지 않습니다 : " + orderableShopId);
        }

        return jpaQueryFactory.select(Projections.constructor(OrderableShopEvent.class,
                orderableShop.id,
                orderableShop.shop.id,
                orderableShop.shop.name,
                eventArticle.id,
                eventArticle.title,
                eventArticle.content,
                eventArticle.startDate,
                eventArticle.endDate)
            )
            .from(orderableShop)
            .innerJoin(orderableShop.shop, shop)
            .innerJoin(shop.eventArticles, eventArticle)
            .where(
                orderableShop.id.eq(orderableShopId),
                orderableShop.isDeleted.isFalse(),
                shop.isDeleted.isFalse(),
                eventArticle.isDeleted.isFalse(),
                eventArticleIsOngoing(today)
            )
            .fetch();
    }

    public Map<Integer, List<EventArticleImage>> getEventArticleImagesMap(List<Integer> eventArticleIds) {
        if (eventArticleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return jpaQueryFactory
            .select(eventArticle.id, eventArticleImage)
            .from(eventArticleImage)
            .innerJoin(eventArticleImage.eventArticle, eventArticle)
            .where(eventArticle.id.in(eventArticleIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(eventArticle.id),
                Collectors.mapping(tuple -> tuple.get(eventArticleImage), Collectors.toList())
            ));
    }

    private BooleanExpression eventArticleIsOngoing(LocalDate today) {
        return eventArticle.startDate.loe(today)
            .and(eventArticle.endDate.goe(today));
    }
}
