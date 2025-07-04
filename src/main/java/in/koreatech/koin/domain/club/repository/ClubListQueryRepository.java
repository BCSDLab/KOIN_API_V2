package in.koreatech.koin.domain.club.repository;

import static in.koreatech.koin.domain.club.model.QClub.club;
import static in.koreatech.koin.domain.club.model.QClubLike.clubLike;
import static in.koreatech.koin.domain.club.model.QClubRecruitment.clubRecruitment;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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
        NumberExpression<Integer> recruitmentPeriod = recruitmentPeriod();
        BooleanBuilder clubFilter = clubSearchFilter(categoryId, isRecruiting, normalizeString(query));
        OrderSpecifier<?> clubSort = clubSort(clubSortType);

        return queryFactory
            .select(Projections.constructor(ClubBaseInfo.class,
                club.id,
                club.name,
                club.clubCategory.name,
                club.likes,
                club.imageUrl,
                clubLike.id.isNotNull(),
                club.isLikeHidden,
                recruitmentPeriod,
                clubRecruitment.isAlwaysRecruiting
            ))
            .from(club)
            .leftJoin(clubRecruitment).on(clubRecruitment.club.id.eq(club.id))
            .leftJoin(clubLike).on(clubLike.club.id.eq(club.id).and(clubLike.user.id.eq(userId)))
            .where(clubFilter)
            .orderBy(clubSort)
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
        }

        // 동아리 활성화 필터
        booleanBuilder.and(club.isActive.isTrue());
        return booleanBuilder;
    }

    private OrderSpecifier<?> clubSort(ClubSortType clubSortType) {
        return clubSortType.getOrderSpecifier();
    }

    private NumberExpression<Integer> recruitmentPeriod() {
        return Expressions.numberTemplate(
            Integer.class,
            "DATEDIFF({0}, {1})",
            clubRecruitment.endDate,
            clubRecruitment.startDate
        );
    }

    private String normalizeString(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }
}
