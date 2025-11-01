package in.koreatech.koin.domain.shop.repository.shop;

import static in.koreatech.koin.domain.shop.model.menu.QMenu.menu;
import static in.koreatech.koin.domain.shop.model.shop.QShop.shop;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.repository.shop.dto.ShopMenuNameKeywordHit;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopNameKeywordHit;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShopSearchQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ShopNameKeywordHit> findAllShopByKeyword(List<String> keywords) {
        return queryFactory
            .select(Projections.constructor(ShopNameKeywordHit.class,
                shop.id,
                shop.name
            ))
            .from(shop)
            .where(shopNameContainsAnyOfKeywords(keywords))
            .fetch();
    }

    public List<ShopMenuNameKeywordHit> findAllShopMenuByKeyword(List<String> keywords) {
        return queryFactory
            .select(Projections.constructor(ShopMenuNameKeywordHit.class,
                shop.id,
                shop.name,
                menu.name
            ))
            .from(menu)
            .innerJoin(menu.shop, shop)
            .where(menuNameContainsAnyOfKeywords(keywords))
            .fetch();
    }

    /**
     * 키워드 리스트 중 하나라도 상점 이름에 포함되는 BooleanExpression을 생성
     * ((shop.name LIKE '%keyword1%' OR shop.internal_name LIKE '%keyword1%') OR ...)
     */
    private BooleanExpression shopNameContainsAnyOfKeywords(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return Expressions.FALSE;
        }

        return keywords.stream()
            .map(keyword -> shop.name.contains(keyword)
                .or(shop.internalName.contains(keyword)))
            .reduce(BooleanExpression::or)
            .orElse(null);
    }

    /**
     * 키워드 리스트 중 하나라도 메뉴 이름에 포함되는 BooleanExpression을 생성
     * (menu.name LIKE '%keyword1%' OR menu.name LIKE '%keyword2%' OR ...)
     */
    private BooleanExpression menuNameContainsAnyOfKeywords(List<String> keywords) {
        if (CollectionUtils.isEmpty(keywords)) {
            return Expressions.FALSE;
        }
        return keywords.stream()
            .map(menu.name::contains)
            .reduce(BooleanExpression::or)
            .orElse(null);
    }
}
