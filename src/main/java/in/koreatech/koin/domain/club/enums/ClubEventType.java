package in.koreatech.koin.domain.club.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClubEventType {
    UPCOMING("행사 예정", 1),
    SOON("곧 행사 진행", 2),
    ONGOING("행사 진행 중", 3),
    ENDED("행사 종료", 4);

    private final String displayName;
    private final int priority;
}
