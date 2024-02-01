package in.koreatech.koin.domain.version.model;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VersionType {
    ANDROID("android"),
    TIMETABLE("timetable"),
    SHUTTLE("shuttle_bus_timetable"),
    CITY("city_bus_timetable"),
    EXPRESS("express_bus_timetable"),
    ;

    private final String value;

    public static VersionType from(String value) {
        return Arrays.stream(values())
            .filter(versionType -> versionType.value.equals(value))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("잘못된 버전 타입입니다.. type: " + value));
    }
}