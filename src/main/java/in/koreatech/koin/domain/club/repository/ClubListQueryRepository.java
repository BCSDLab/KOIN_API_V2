package in.koreatech.koin.domain.club.repository;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;
import static in.koreatech.koin.domain.club.model.QClub.club;
import static in.koreatech.koin.domain.club.model.QClubLike.clubLike;
import static in.koreatech.koin.domain.club.model.QClubRecruitment.clubRecruitment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.domain.club.enums.ClubSortType;
import in.koreatech.koin.domain.club.model.ClubBaseInfo;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ClubListQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<ClubBaseInfo> findAllClubInfo(
        Integer categoryId, ClubSortType sortType, Boolean isRecruiting, String query, Integer userId
    ) {
        BooleanBuilder where = buildClubFilter(categoryId, isRecruiting, normalize(query));
        List<OrderSpecifier<?>> orderSpecifiers = buildSortOrders(sortType, isRecruiting);
        BooleanExpression isLiked = buildIsLikedCondition(userId);

        var baseQuery = queryFactory
            .select(Projections.constructor(ClubBaseInfo.class,
                club.id,
                club.name,
                club.clubCategory.name,
                club.likes,
                club.imageUrl,
                isLiked,
                club.isLikeHidden,
                clubRecruitment.startDate,
                clubRecruitment.endDate,
                clubRecruitment.isAlwaysRecruiting
            ))
            .from(club)
            .leftJoin(clubRecruitment).on(clubRecruitment.club.id.eq(club.id));

        if (userId != null) {
            baseQuery.leftJoin(clubLike).on(
                clubLike.club.id.eq(club.id)
                    .and(clubLike.user.id.eq(userId))
            );
        }

        return baseQuery
            .where(where)
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .fetch();
    }

    private BooleanBuilder buildClubFilter(Integer categoryId, Boolean isRecruiting, String normalizedQuery) {
        BooleanBuilder builder = new BooleanBuilder();

        if (categoryId != null) {
            builder.and(club.clubCategory.id.eq(categoryId));
        }

        if (isRecruiting) {
            builder.and(clubRecruitment.id.isNotNull());
            builder.and(
                clubRecruitment.endDate.goe(LocalDate.now())
                    .or(clubRecruitment.isAlwaysRecruiting.isTrue())
            );
        }

        builder.and(Expressions.stringTemplate("LOWER(REPLACE({0}, ' ', ''))", club.name).contains(normalizedQuery));
        builder.and(club.isActive.isTrue());

        return builder;
    }

    private List<OrderSpecifier<?>> buildSortOrders(ClubSortType sortType, Boolean isRecruiting) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        orders.add(sortType.getOrderSpecifier());

        if (isRecruiting) {
            orders.add(clubRecruitment.createdAt.desc());

            NumberTemplate<Integer> deadlineGap = numberTemplate(
                Integer.class,
                "datediff({0}, current_date)",
                clubRecruitment.endDate
            );
            orders.add(deadlineGap.asc());

            orders.add(clubRecruitment.isAlwaysRecruiting.desc());
        }

        return orders;
    }

    private BooleanExpression buildIsLikedCondition(Integer userId) {
        if (userId != null) {
            return clubLike.id.isNotNull();
        }
        return Expressions.asBoolean(false).isTrue();
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }
}
