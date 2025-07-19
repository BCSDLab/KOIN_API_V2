package in.koreatech.koin.domain.club.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubEventType {
    RECENT("최신 등록순", 0),
    ONGOING("행사 진행 중", 1),
    UPCOMING("행사 예정", 2),
    ENDED("종료된 행사", 3)
    ;

    private final String displayName;
    private final int priority;
}
