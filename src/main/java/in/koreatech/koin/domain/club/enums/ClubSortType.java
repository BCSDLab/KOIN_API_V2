package in.koreatech.koin.domain.club.enums;

import static in.koreatech.koin.domain.club.model.QClub.club;
import static in.koreatech.koin.domain.club.model.QClubRecruitment.clubRecruitment;

import java.util.List;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;

import lombok.Getter;

@Getter
public enum ClubSortType {
    NONE {
        // TODO. 향후 해당 필드를 사용하지 않는 경우 빈 리스트로 수정
        @Override
        public List<OrderSpecifier<?>> getOrderSpecifiers() {
            return List.of(club.createdAt.asc());
        }
    },
    CREATED_AT_ASC {
        @Override
        public List<OrderSpecifier<?>> getOrderSpecifiers() {
            return List.of(club.createdAt.asc());
        }
    },
    HITS_DESC {
        @Override
        public List<OrderSpecifier<?>> getOrderSpecifiers() {
            return List.of(club.hits.desc());
        }
    },
    RECRUITMENT_UPDATED_DESC {
        @Override
        public List<OrderSpecifier<?>> getOrderSpecifiers() {
            return List.of(clubRecruitment.updatedAt.desc());
        }
    },
    RECRUITING_DEADLINE_ASC {
        @Override
        public List<OrderSpecifier<?>> getOrderSpecifiers() {
            NumberTemplate<Integer> deadlineGap = Expressions.numberTemplate(
                Integer.class,
                "datediff({0}, current_date)",
                clubRecruitment.endDate
            );

            return List.of(
                deadlineGap.asc(),
                clubRecruitment.isAlwaysRecruiting.desc()
            );
        }
    };

    public abstract List<OrderSpecifier<?>> getOrderSpecifiers();
}
