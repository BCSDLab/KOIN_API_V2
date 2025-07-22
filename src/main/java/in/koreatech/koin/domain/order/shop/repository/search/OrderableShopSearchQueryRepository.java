package in.koreatech.koin.domain.order.shop.repository.search;

import static in.koreatech.koin.domain.order.shop.model.entity.menu.QOrderableShopMenu.orderableShopMenu;
import static in.koreatech.koin.domain.order.shop.model.entity.shop.QOrderableShop.orderableShop;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse.InnerMenuNameSearchRelatedKeywordResult;
import in.koreatech.koin.domain.order.shop.dto.shopsearch.OrderableShopSearchRelatedKeywordResponse.InnerShopNameSearchRelatedKeywordResult;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderableShopSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<InnerShopNameSearchRelatedKeywordResult> findAllOrderableShopByKeyword(String keyword) {
        return queryFactory
            .select(Projections.constructor(InnerShopNameSearchRelatedKeywordResult.class,
                orderableShop.id,
                shop.name
            ))
            .from(shop)
            .innerJoin(orderableShop).on(orderableShop.shop.id.eq(shop.id))
            .where(shop.name.contains(keyword))
            .fetch();
    }

    public List<InnerMenuNameSearchRelatedKeywordResult> findAllMenuByKeyword(String keyword) {
        return queryFactory
            .select(Projections.constructor(InnerMenuNameSearchRelatedKeywordResult.class,
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
}
