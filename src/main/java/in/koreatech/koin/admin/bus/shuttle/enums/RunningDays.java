package in.koreatech.koin.admin.bus.shuttle.enums;

import java.util.List;

import in.koreatech.koin.admin.bus.shuttle.model.RouteInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RunningDays {
    WEEKDAYS("주중", List.of("MON", "TUE", "WED", "THU", "FRI")),
    THURSDAY_FRIDAY("목금", List.of("THU", "FRI")),
    SATURDAY("토요일", List.of("SAT")),
    SUNDAY("일요일", List.of("SUN"));

    private final String description;
    private final List<String> days;

    public static RunningDays from(RouteInfo.InnerNameDetail innerNameDetail) {
        String name = innerNameDetail.getName();

        if (name.contains("목") && name.contains("금")) {
            return THURSDAY_FRIDAY;
        } else if (name.contains("토")) {
            return SATURDAY;
        } else if (name.contains("일")) {
            return SUNDAY;
        }

        return WEEKDAYS;
    }
}