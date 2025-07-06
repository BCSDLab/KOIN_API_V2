package in.koreatech.koin.domain.club.enums;

import static in.koreatech.koin._common.code.ApiResponseCode.NOT_ALLOWED_RECRUITING_SORT_TYPE;
import static in.koreatech.koin.domain.club.model.QClub.club;
import static in.koreatech.koin.domain.club.model.QClubRecruitment.clubRecruitment;

import java.util.List;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;

import in.koreatech.koin._common.exception.CustomException;
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

        @Override
        public boolean isRecruitingOnly() {
            return true;
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

        @Override
        public boolean isRecruitingOnly() {
            return true;
        }
    };

    public abstract List<OrderSpecifier<?>> getOrderSpecifiers();

    private boolean isRecruitingOnly() {
        return false;
    }

    public void validateRecruitingCondition(boolean isRecruiting) {
        if (this.isRecruitingOnly() && !isRecruiting) {
            throw CustomException.of(NOT_ALLOWED_RECRUITING_SORT_TYPE);
        }
    }
}
