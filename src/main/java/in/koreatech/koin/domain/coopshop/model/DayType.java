package in.koreatech.koin.domain.coopshop.model;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_COOP_SHOP_DAY_OF_WEEK;

import java.util.Arrays;

import in.koreatech.koin.global.exception.CustomException;
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
            .orElseThrow(() -> CustomException.of(INVALID_COOP_SHOP_DAY_OF_WEEK));
    }
}
