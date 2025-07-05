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
        Integer categoryId, ClubSortType clubSortType, Boolean isRecruiting, String query, Integer userId
    ) {
        BooleanBuilder clubFilter = clubSearchFilter(categoryId, isRecruiting, normalizeString(query));
        List<OrderSpecifier<?>> clubSort = clubSort(clubSortType, isRecruiting);

        BooleanExpression isLiked = (userId != null)
            ? clubLike.id.isNotNull()
            : Expressions.asBoolean(false).isTrue();

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
            baseQuery.leftJoin(clubLike).on(clubLike.club.id.eq(club.id).and(clubLike.user.id.eq(userId)));
        }

        return baseQuery
            .where(clubFilter)
            .orderBy(clubSort.toArray(new OrderSpecifier[0]))
            .fetch();
    }

    private BooleanBuilder clubSearchFilter(Integer categoryId, Boolean isRecruiting, String query) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        // 동아리 카테고리 필터
        if (categoryId != null) {
            booleanBuilder.and(club.clubCategory.id.eq(categoryId));
        }

        // 동아리 검색어 필터
        booleanBuilder.and(Expressions.stringTemplate("LOWER(REPLACE({0}, ' ', ''))", club.name).contains(query));

        // 동아리 모집 필터
        if (isRecruiting) {
            booleanBuilder.and(clubRecruitment.id.isNotNull());
            booleanBuilder.and(clubRecruitment.endDate.goe(LocalDate.now()).or(clubRecruitment.isAlwaysRecruiting.isTrue()));
        }

        // 동아리 활성화 필터
        booleanBuilder.and(club.isActive.isTrue());
        return booleanBuilder;
    }

    private List<OrderSpecifier<?>> clubSort(ClubSortType clubSortType, Boolean isRecruiting) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        orders.add(clubSortType.getOrderSpecifier());

        if (isRecruiting) {
            orders.add(clubRecruitment.startDate.desc());
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

    private String normalizeString(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }
}
