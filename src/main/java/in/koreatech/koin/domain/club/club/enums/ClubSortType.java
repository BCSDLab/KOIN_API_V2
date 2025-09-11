package in.koreatech.koin.domain.club.club.enums;

import static in.koreatech.koin.domain.club.club.model.QClub.club;
import static in.koreatech.koin.domain.club.recruitment.model.QClubRecruitment.clubRecruitment;
import static in.koreatech.koin.global.code.ApiResponseCode.NOT_ALLOWED_RECRUITING_SORT_TYPE;

import java.util.List;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;

import in.koreatech.koin.global.exception.CustomException;
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
            /**
             * 우선순위
             * 1. 마감일이 짧은 동아리
             * 2. 상시 모집인 동아리
             * 3. 모집 전인 동아리
             */
            NumberTemplate<Integer> recruitmentStatusPriority = Expressions.numberTemplate(
                Integer.class,
                """
                    case
                        when {0} = true then 1
                        when {1} > current_date then 2
                        else 0
                    end
                    """,
                clubRecruitment.isAlwaysRecruiting,
                clubRecruitment.startDate
            );

            NumberTemplate<Integer> deadlineGap = Expressions.numberTemplate(
                Integer.class,
                "datediff({0}, current_date)",
                clubRecruitment.endDate
            );

            return List.of(recruitmentStatusPriority.asc(), deadlineGap.asc());
        }

        @Override
        public boolean isRecruitingOnly() {
            return true;
        }
    };

    public abstract List<OrderSpecifier<?>> getOrderSpecifiers();

    public boolean isRecruitingOnly() {
        return false;
    }

    public void validateRecruitingCondition(boolean isRecruiting) {
        if (this.isRecruitingOnly() && !isRecruiting) {
            throw CustomException.of(NOT_ALLOWED_RECRUITING_SORT_TYPE);
        }
    }
}
