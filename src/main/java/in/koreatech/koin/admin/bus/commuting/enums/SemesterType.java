package in.koreatech.koin.admin.bus.commuting.enums;

import lombok.Getter;

@Getter
public enum SemesterType {
    REGULAR("정규학기"),
    SEASONAL("계절학기"),
    VACATION("방학기간"),
    ;

    private final String description;

    SemesterType(String description) {
        this.description = description;
    }
}
