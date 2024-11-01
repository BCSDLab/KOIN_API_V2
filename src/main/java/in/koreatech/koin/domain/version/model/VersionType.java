package in.koreatech.koin.domain.version.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VersionType {
    ANDROID("android", "PLATFORM"),
    IOS("ios", "PLATFORM"),
    ANDROID_OWNER("android_owner", "PLATFORM"),
    TIMETABLE("timetable", "OTHER"),
    SHUTTLE("shuttle_bus_timetable", "OTHER"),
    CITY("city_bus_timetable", "OTHER"),
    EXPRESS("express_bus_timetable", "OTHER"),
    ;

    private final String value;
    private final String category;

    @JsonCreator
    public static VersionType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("versionType: " + value));
    }

    public boolean isPlatform() {
        return this.category.equals("PLATFORM");
    }
}
