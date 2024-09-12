package in.koreatech.koin.domain.shop.repository.shop;

import static in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse.InnerShopOpen;
import static in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse.InnerShopResponse;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;
import in.koreatech.koin.domain.benefit.model.QBenefitCategoryMap;
import in.koreatech.koin.domain.shop.model.article.QEventArticle;
import in.koreatech.koin.domain.shop.model.shop.QShop;
import in.koreatech.koin.domain.shop.model.shop.QShopOpen;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepositoryImpl implements ShopCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public BenefitShopsResponse getBenefitShops(Integer benefit, LocalDate now) {
        QBenefitCategoryMap benefitCategoryMap = QBenefitCategoryMap.benefitCategoryMap;
        QShop shop = benefitCategoryMap.shop;
        QShopOpen shopOpens = QShopOpen.shopOpen;
        BooleanBuilder whereCondition = new BooleanBuilder();
        whereCondition.and(benefitCategoryMap.benefitCategory.id.eq(benefit));

        List<InnerShopResponse> innerShopResponses = queryFactory
            .select(Projections.constructor(InnerShopResponse.class,
                shop.delivery,
                shop.id,
                shop.name,
                Projections.list(Projections.constructor(InnerShopOpen.class,
                    shopOpens.dayOfWeek,
                    shopOpens.closed,
                    shopOpens.openTime,
                    shopOpens.closeTime
                ))

                // 결제 관련 정보
                shop.payBank,
                shop.payCard,

                // 전화번호
                shop.phone,

                // 이벤트 여부
                isEvent(shop.id, now).as("isEvent")
            )
            .from(benefitCategoryMap)
            .leftJoin(shop.shopOpens).fetchJoin()
            .leftJoin(shop.shopCategories).fetchJoin()
            .fetch();

        return new BenefitShopsResponse(
            innerShopResponses.size(),
            innerShopResponses
        );
    }

    private BooleanExpression isEvent(NumberPath<Integer> shopId, LocalDate now) {
        QEventArticle eventArticle = QEventArticle.eventArticle;
        return eventArticle.shop.id.eq(shopId)
            .and(eventArticle.startDate.loe(now))
            .and(eventArticle.endDate.goe(now));
    }
}
