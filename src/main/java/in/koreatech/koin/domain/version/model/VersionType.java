package in.koreatech.koin.domain.version.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VersionType {
    ANDROID("android"),
    IOS("ios"),
    TIMETABLE("timetable"),
    SHUTTLE("shuttle_bus_timetable"),
    CITY("city_bus_timetable"),
    EXPRESS("express_bus_timetable"),
    ;

    private final String value;

    @JsonCreator
    public static VersionType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("versionType: " + value));
    }
}
