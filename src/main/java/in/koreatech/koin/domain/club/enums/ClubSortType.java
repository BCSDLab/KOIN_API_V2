package in.koreatech.koin.domain.club.enums;

import static in.koreatech.koin.domain.club.model.QClub.club;

import java.time.LocalDateTime;

import com.querydsl.core.types.OrderSpecifier;

import lombok.Getter;

@Getter
public enum ClubSortType {
    NONE {
        @Override
        public OrderSpecifier<LocalDateTime> getOrderSpecifier() {
            return club.createdAt.asc();
        }
    },
    HITS_DESC {
        @Override
        public OrderSpecifier<Integer> getOrderSpecifier() {
            return club.hits.desc();
        }
    };

    public abstract OrderSpecifier<?> getOrderSpecifier();
}
