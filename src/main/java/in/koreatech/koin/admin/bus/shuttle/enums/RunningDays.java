package in.koreatech.koin.admin.bus.shuttle.enums;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.koreatech.koin.admin.bus.shuttle.model.ShuttleBusTimetable.RouteInfo.InnerNameDetail;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RunningDays {
    WEEKDAYS("주중", List.of("MON", "TUE", "WED", "THU", "FRI")),
    THURSDAY_FRIDAY("목금", List.of("THU", "FRI")),
    SATURDAY("토요일", List.of("SAT")),
    SUNDAY("일요일", List.of("SUN")),
    MONDAY("월요일", List.of("MON")),
    FRIDAY("금요일", List.of("FRI")),
    TUESDAY("화요일", List.of("TUE")),
    WEDNESDAY("수요일", List.of("WED")),
    THURSDAY("목요일", List.of("THU"));

    private static final Pattern THURSDAY_FRIDAY_PATTERN = Pattern.compile(
        "(?<![가-힣])목(?:요일)?(?:[\\s·・,，/／\\-~]+)?금(?:요일)?(?=추가|[^가-힣]|$)"
    );
    private static final Pattern EXPLICIT_WEEKDAY_PATTERN = Pattern.compile(
        "(월요일|화요일|수요일|목요일|금요일|토요일|일요일)"
    );
    private static final Map<String, RunningDays> EXPLICIT_WEEKDAYS = Map.of(
        "월요일", MONDAY,
        "화요일", TUESDAY,
        "수요일", WEDNESDAY,
        "목요일", THURSDAY,
        "금요일", FRIDAY,
        "토요일", SATURDAY,
        "일요일", SUNDAY
    );

    private final String description;
    private final List<String> days;

    public static RunningDays from(InnerNameDetail innerNameDetail) {
        if (innerNameDetail == null) {
            return WEEKDAYS;
        }

        String description = String.join(" ",
            valueOrEmpty(innerNameDetail.getName()),
            valueOrEmpty(innerNameDetail.getDetail())
        ).trim();

        Set<RunningDays> explicitDays = findExplicitDays(description);
        if (THURSDAY_FRIDAY_PATTERN.matcher(description).find()) {
            boolean hasConflict = explicitDays.stream()
                .anyMatch(day -> day != THURSDAY && day != FRIDAY);
            if (hasConflict) {
                throw conflictingDays(description);
            }
            return THURSDAY_FRIDAY;
        }

        if (explicitDays.size() == 1) {
            return explicitDays.iterator().next();
        }

        if (explicitDays.size() > 1) {
            throw conflictingDays(description);
        }

        return WEEKDAYS;
    }

    private static Set<RunningDays> findExplicitDays(String description) {
        Set<RunningDays> explicitDays = EnumSet.noneOf(RunningDays.class);
        Matcher matcher = EXPLICIT_WEEKDAY_PATTERN.matcher(description);
        while (matcher.find()) {
            explicitDays.add(EXPLICIT_WEEKDAYS.get(matcher.group(1)));
        }
        return explicitDays;
    }

    private static CustomException conflictingDays(String description) {
        return CustomException.of(ApiResponseCode.INVALID_REQUEST_BODY,
            "운행 요일이 서로 충돌합니다: " + description);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
