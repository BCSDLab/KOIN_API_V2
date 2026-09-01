package in.koreatech.koin.domain.team.recruitment.repository;

import static in.koreatech.koin.domain.team.recruitment.model.QTeamRecruitment.teamRecruitment;
import static in.koreatech.koin.domain.team.recruitment.model.QTeamRecruitmentRole.teamRecruitmentRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentDisplayName;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentSort;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatusFilter;
import in.koreatech.koin.domain.team.recruitment.model.TeamRecruitment;
import lombok.RequiredArgsConstructor;

/**
 * 모집글 목록 조회. 필터 조합이 많아 QueryDSL 로 분리했다.
 * 역할 목록을 함께 쓰기 때문에, 페이지 대상 id 를 먼저 뽑고 그 id 들만 fetch join 으로 다시 읽는다.
 */
@Repository
@RequiredArgsConstructor
public class TeamRecruitmentListQueryRepository {

    private final JPAQueryFactory queryFactory;

    public long count(
        String keyword,
        TeamRecruitmentStatusFilter statusFilter,
        List<TeamRecruitmentCategory> categories,
        TeamRecruitmentMeetingType meetingType
    ) {
        Long total = queryFactory
            .select(teamRecruitment.count())
            .from(teamRecruitment)
            .where(publicFilter(keyword, statusFilter, categories, meetingType))
            .fetchOne();
        return total == null ? 0L : total;
    }

    public List<TeamRecruitment> findAll(
        String keyword,
        TeamRecruitmentStatusFilter statusFilter,
        List<TeamRecruitmentCategory> categories,
        TeamRecruitmentMeetingType meetingType,
        TeamRecruitmentSort sort,
        Criteria criteria
    ) {
        return findByIdsInOrder(
            pageIds(publicFilter(keyword, statusFilter, categories, meetingType), sort, criteria),
            sort
        );
    }

    public long countByAuthor(Integer authorId, TeamRecruitmentStatusFilter statusFilter) {
        Long total = queryFactory
            .select(teamRecruitment.count())
            .from(teamRecruitment)
            .where(authorFilter(authorId, statusFilter))
            .fetchOne();
        return total == null ? 0L : total;
    }

    public List<TeamRecruitment> findAllByAuthor(
        Integer authorId,
        TeamRecruitmentStatusFilter statusFilter,
        TeamRecruitmentSort sort,
        Criteria criteria
    ) {
        return findByIdsInOrder(pageIds(authorFilter(authorId, statusFilter), sort, criteria), sort);
    }

    private List<Integer> pageIds(BooleanBuilder filter, TeamRecruitmentSort sort, Criteria criteria) {
        return queryFactory
            .select(teamRecruitment.id)
            .from(teamRecruitment)
            .where(filter)
            .orderBy(orderBy(sort))
            .offset((long) criteria.getPage() * criteria.getLimit())
            .limit(criteria.getLimit())
            .fetch();
    }

    private List<TeamRecruitment> findByIdsInOrder(List<Integer> ids, TeamRecruitmentSort sort) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(teamRecruitment)
            .leftJoin(teamRecruitment.roles).fetchJoin()
            .where(teamRecruitment.id.in(ids))
            .orderBy(orderBy(sort))
            .distinct()
            .fetch();
    }

    private BooleanBuilder publicFilter(
        String keyword,
        TeamRecruitmentStatusFilter statusFilter,
        List<TeamRecruitmentCategory> categories,
        TeamRecruitmentMeetingType meetingType
    ) {
        BooleanBuilder filter = new BooleanBuilder(teamRecruitment.status.in(statuses(statusFilter)));
        String normalized = normalize(keyword);
        if (normalized != null) {
            filter.and(keywordFilter(normalized));
        }
        if (categories != null && !categories.isEmpty()) {
            filter.and(teamRecruitment.category.in(categories.stream().filter(Objects::nonNull).toList()));
        }
        if (meetingType != null) {
            filter.and(teamRecruitment.meetingType.eq(meetingType));
        }
        return filter;
    }

    /**
     * 검색어는 모집글 제목, 역할명, 카테고리 표시명, 진행 방식 표시명을 대상으로 부분 일치 비교한다.
     * 본문(description)은 대상이 아니다.
     * 역할은 exists 로 확인해 조인 때문에 모집글이 중복되지 않게 한다.
     */
    private BooleanBuilder keywordFilter(String keyword) {
        BooleanBuilder keywordFilter = new BooleanBuilder(teamRecruitment.title.containsIgnoreCase(keyword));
        keywordFilter.or(JPAExpressions.selectOne()
            .from(teamRecruitmentRole)
            .where(teamRecruitmentRole.recruitment.id.eq(teamRecruitment.id)
                .and(teamRecruitmentRole.name.containsIgnoreCase(keyword)))
            .exists());

        List<TeamRecruitmentCategory> categories = TeamRecruitmentDisplayName.categoriesMatching(keyword);
        if (!categories.isEmpty()) {
            keywordFilter.or(teamRecruitment.category.in(categories));
        }
        List<TeamRecruitmentMeetingType> meetingTypes = TeamRecruitmentDisplayName.meetingTypesMatching(keyword);
        if (!meetingTypes.isEmpty()) {
            keywordFilter.or(teamRecruitment.meetingType.in(meetingTypes));
        }
        return keywordFilter;
    }

    private BooleanBuilder authorFilter(Integer authorId, TeamRecruitmentStatusFilter statusFilter) {
        return new BooleanBuilder(teamRecruitment.author.id.eq(authorId))
            .and(teamRecruitment.status.in(statuses(statusFilter)));
    }

    private List<TeamRecruitmentStatus> statuses(TeamRecruitmentStatusFilter statusFilter) {
        TeamRecruitmentStatusFilter resolved =
            statusFilter == null ? TeamRecruitmentStatusFilter.ALL : statusFilter;
        return resolved.getStatuses();
    }

    private OrderSpecifier<?>[] orderBy(TeamRecruitmentSort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        if (sort == TeamRecruitmentSort.DEADLINE_ASC) {
            orders.add(teamRecruitment.deadlineDate.asc());
            orders.add(teamRecruitment.id.asc());
        } else {
            orders.add(teamRecruitment.createdAt.desc());
            orders.add(teamRecruitment.id.desc());
        }
        return orders.toArray(new OrderSpecifier[0]);
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
