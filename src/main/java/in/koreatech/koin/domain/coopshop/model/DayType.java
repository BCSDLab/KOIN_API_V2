package in.koreatech.koin.domain.coopshop.model;

import java.util.Arrays;

import in.koreatech.koin.domain.coopshop.exception.DayTypeNotFoundException;
import lombok.Getter;

@Getter
public enum DayType {
    WEEKDAYS("평일"),
    WEEKEND("주말"),
    MONDAY("월요일"),
    TUESDAY("화요일"),
    WEDNESDAY("수요일"),
    THURSDAY("목요일"),
    FRIDAY("금요일"),
    SATURDAY("토요일"),
    SUNDAY("일요일");

    private final String day;

    DayType(String day) {
        this.day = day;
    }

    public static DayType from(String day) {
        return Arrays.stream(DayType.values())
            .filter(dayType -> dayType.getDay().equals(day))
            .findAny()
            .orElseThrow(() -> new DayTypeNotFoundException(day));
    }
}
