package in.koreatech.koin.admin.shop.repository.review;

import static in.koreatech.koin.domain.shop.model.review.ReportStatus.UNHANDLED;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.shop.model.review.QShopReview;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminShopReviewCustomRepositoryImpl implements AdminShopReviewCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ShopReview> findShopReview(
        Integer shopId,
        Boolean isReported,
        Boolean isHaveUnhandledReport,
        Pageable pageable
    ) {
        QShopReview shopReview = QShopReview.shopReview;
        BooleanBuilder whereCondition = buildWhereCondition(shopId, isReported, isHaveUnhandledReport);

        List<ShopReview> shopReviews = queryFactory
            .selectDistinct(shopReview)
            .from(shopReview)
            .leftJoin(shopReview.shop).fetchJoin()
            .leftJoin(shopReview.reports).fetchJoin()
            .where(whereCondition)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = this.countShopReview(shopId, isReported, isHaveUnhandledReport);
        return new PageImpl<>(shopReviews, pageable, total);
    }

    @Override
    public Long countShopReview(
        Integer shopId,
        Boolean isReported,
        Boolean isHaveUnhandledReport
    ) {
        QShopReview shopReview = QShopReview.shopReview;
        BooleanBuilder whereCondition = buildWhereCondition(shopId, isReported, isHaveUnhandledReport);

        return queryFactory
            .select(shopReview.count())
            .from(shopReview)
            .where(whereCondition)
            .fetchOne();
    }

    private BooleanBuilder buildWhereCondition(
        Integer shopId,
        Boolean isReported,
        Boolean isHaveUnhandledReport
    ) {
        QShopReview shopReview = QShopReview.shopReview;
        BooleanBuilder whereCondition = new BooleanBuilder();

        if (shopId != null) {
            whereCondition.and(shopReview.shop.id.eq(shopId));
        }

        if (isReported != null) {
            if (isReported) {
                whereCondition.and(shopReview.reports.isNotEmpty());
            } else {
                whereCondition.and(shopReview.reports.isEmpty());
            }
        }

        if (isHaveUnhandledReport != null) {
            if (isHaveUnhandledReport) {
                whereCondition.and(shopReview.reports.any().reportStatus.eq(UNHANDLED));
            }
        }

        whereCondition.and(shopReview.isDeleted.eq(false));
        return whereCondition;
    }
}
