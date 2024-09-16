package in.koreatech.koin.domain.shop.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.dto.ShopResponse;
import in.koreatech.koin.domain.shop.dto.ShopsResponseV2;
import in.koreatech.koin.domain.shop.model.QEventArticle;
import in.koreatech.koin.domain.shop.model.QShop;
import in.koreatech.koin.domain.shop.model.QShopCategory;
import in.koreatech.koin.domain.shop.model.QShopCategoryMap;
import in.koreatech.koin.domain.shop.model.QShopOpen;
import in.koreatech.koin.domain.shop.model.QShopReview;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepositoryImpl implements ShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    public ShopsResponseV2 getShops(LocalDate now) {
        QShop shop = QShop.shop;
        QShopOpen shopOpen = QShopOpen.shopOpen;
        QShopCategoryMap shopCategoryMap = QShopCategoryMap.shopCategoryMap;
        QShopReview shopReview = QShopReview.shopReview;

        List<ShopsResponseV2.InnerShopResponse> shopResponses = queryFactory
            .select(Projections.bean(ShopsResponseV2.InnerShopResponse.class,
                shop.delivery,
                shop.id,
                shop.name,
                shop.payCard,
                shop.payBank,
                shop.phone,
                isEvent(shop.id, now),
                Projections.constructor(ShopResponse.InnerShopOpen.class,
                    shopOpen.openTime,
                    shopOpen.closeTime,
                    shopOpen.closed,
                    shopOpen.dayOfWeek
                ).as("shopOpens"),
                shopCategoryMap.shopCategory.id.as("shopCategories")
            ))
            .from(shop)
            .leftJoin(shop.shopOpens).fetchJoin()
            .leftJoin(shop.shopCategories).fetchJoin()
            .leftJoin(shop.reviews).fetchJoin()
            .fetch();

        return new ShopsResponseV2(
            shopResponses.size(),
            shopResponses
        );
    }

    private BooleanExpression isEvent(NumberPath<Integer> shopId, LocalDate now) {
        QEventArticle eventArticle = QEventArticle.eventArticle;
        return JPAExpressions
            .selectOne()
            .from(eventArticle)
            .where(eventArticle.shop.id.eq(shopId)
                .and(eventArticle.startDate.loe(now))
                .and(eventArticle.endDate.goe(now))
            ).exists();
    }
}
