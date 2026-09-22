package in.koreatech.koin.domain.callvan.repository;

import static in.koreatech.koin.domain.callvan.model.QCallvanParticipant.callvanParticipant;
import static in.koreatech.koin.domain.callvan.model.QCallvanPost.callvanPost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.callvan.model.CallvanPost;
import in.koreatech.koin.domain.callvan.model.enums.CallvanLocation;
import in.koreatech.koin.domain.callvan.model.enums.CallvanStatus;
import in.koreatech.koin.domain.callvan.model.filter.CallvanPostSortCriteria;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CallvanPostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<CallvanPost> findCallvanPosts(
        Integer authorId,
        List<CallvanLocation> departures,
        String departureKeyword,
        List<CallvanLocation> arrivals,
        String arrivalKeyword,
        List<CallvanStatus> statuses,
        String title,
        Integer joinedMemberId,
        CallvanPostSortCriteria sort,
        Criteria criteria,
        LocalDateTime now
    ) {
        return queryFactory
            .selectFrom(callvanPost)
            .where(
                authorEq(authorId),
                departureFilter(departures, departureKeyword),
                arrivalFilter(arrivals, arrivalKeyword),
                statusIn(statuses),
                titleContains(title),
                joinedByMemberId(joinedMemberId),
                notExpired(authorId, joinedMemberId, now))
            .orderBy(getOrderSpecifiers(sort))
            .offset((long)criteria.getPage() * criteria.getLimit())
            .limit(criteria.getLimit())
            .fetch();
    }

    public Long countCallvanPosts(
        Integer authorId,
        List<CallvanLocation> departures,
        String departureKeyword,
        List<CallvanLocation> arrivals,
        String arrivalKeyword,
        List<CallvanStatus> statuses,
        String title,
        Integer joinedMemberId,
        LocalDateTime now
    ) {
        return queryFactory
            .select(callvanPost.count())
            .from(callvanPost)
            .where(
                authorEq(authorId),
                departureFilter(departures, departureKeyword),
                arrivalFilter(arrivals, arrivalKeyword),
                statusIn(statuses),
                titleContains(title),
                joinedByMemberId(joinedMemberId),
                notExpired(authorId, joinedMemberId, now))
            .fetchOne();
    }

    private BooleanExpression authorEq(Integer authorId) {
        return authorId != null ? callvanPost.author.id.eq(authorId) : null;
    }

    private BooleanExpression departureFilter(List<CallvanLocation> departures, String departureKeyword) {
        if (departures == null || departures.isEmpty()) {
            return null;
        }

        BooleanExpression expression = null;

        List<CallvanLocation> normalLocations = departures.stream()
            .filter(loc -> loc != CallvanLocation.CUSTOM)
            .collect(Collectors.toList());

        if (!normalLocations.isEmpty()) {
            expression = callvanPost.departureType.in(normalLocations);
        }

        if (departures.contains(CallvanLocation.CUSTOM) && departureKeyword != null) {
            BooleanExpression customExpression = callvanPost.departureType.eq(CallvanLocation.CUSTOM)
                .and(callvanPost.departureCustomName.containsIgnoreCase(departureKeyword));

            expression = expression == null ? customExpression : expression.or(customExpression);
        }

        return expression;
    }

    private BooleanExpression arrivalFilter(List<CallvanLocation> arrivals, String arrivalKeyword) {
        if (arrivals == null || arrivals.isEmpty()) {
            return null;
        }

        BooleanExpression expression = null;

        List<CallvanLocation> normalLocations = arrivals.stream()
            .filter(loc -> loc != CallvanLocation.CUSTOM)
            .collect(Collectors.toList());

        if (!normalLocations.isEmpty()) {
            expression = callvanPost.arrivalType.in(normalLocations);
        }

        if (arrivals.contains(CallvanLocation.CUSTOM) && arrivalKeyword != null) {
            BooleanExpression customExpression = callvanPost.arrivalType.eq(CallvanLocation.CUSTOM)
                .and(callvanPost.arrivalCustomName.containsIgnoreCase(arrivalKeyword));

            expression = expression == null ? customExpression : expression.or(customExpression);
        }

        return expression;
    }

    private BooleanExpression statusIn(List<CallvanStatus> statuses) {
        return statuses != null && !statuses.isEmpty() ? callvanPost.status.in(statuses) : null;
    }

    private BooleanExpression titleContains(String title) {
        return (title != null && !title.isBlank()) ? callvanPost.title.contains(title) : null;
    }

    private BooleanExpression notExpired(Integer authorId, Integer joinedMemberId, LocalDateTime now) {
        if (authorId != null || joinedMemberId != null) {
            return null;
        }
        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();
        BooleanExpression stillBeforeDeparture = callvanPost.departureDate.gt(today)
            .or(callvanPost.departureDate.eq(today)
                .and(callvanPost.departureTime.gt(nowTime)));
        // CLOSED/COMPLETED 게시글은 출발 시간이 지났어도 정착된 기록이므로 계속 노출한다.
        // RECRUITING 상태에서 출발 시간만 지난, 마감 처리를 놓친 게시글만 이 필터로 숨긴다.
        return callvanPost.status.ne(CallvanStatus.RECRUITING).or(stillBeforeDeparture);
    }

    private BooleanExpression joinedByMemberId(Integer joinedMemberId) {
        if (joinedMemberId == null) {
            return null;
        }

        return JPAExpressions
            .selectOne()
            .from(callvanParticipant)
            .where(
                callvanParticipant.post.id.eq(callvanPost.id),
                callvanParticipant.member.id.eq(joinedMemberId),
                callvanParticipant.isDeleted.isFalse()
            )
            .exists();
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(CallvanPostSortCriteria sort) {
        if (sort == null) {
            return new OrderSpecifier[] {
                callvanPost.createdAt.desc(),
                callvanPost.id.desc()
            };
        }

        return switch (sort) {
            case DEPARTURE_ASC -> new OrderSpecifier[] {
                callvanPost.departureDate.asc(),
                callvanPost.departureTime.asc(),
                callvanPost.id.desc()
            };
            case DEPARTURE_DESC -> new OrderSpecifier[] {
                callvanPost.departureDate.desc(),
                callvanPost.departureTime.desc(),
                callvanPost.id.desc()
            };
            case LATEST_ASC -> new OrderSpecifier[] {
                callvanPost.createdAt.asc(),
                callvanPost.id.asc()
            };
            case LATEST_DESC -> new OrderSpecifier[] {
                callvanPost.createdAt.desc(),
                callvanPost.id.desc()
            };
        };
    }
}
