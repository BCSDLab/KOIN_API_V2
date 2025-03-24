package in.koreatech.koin.domain.banner.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.bus.exception.BusTypeNotFoundException;

public enum PlatformType {
    WEB,
    ANDROID,
    IOS,
    ;

    @JsonCreator
    public static PlatformType from(String platform) {
        return Arrays.stream(values())
            .filter(platformType -> platformType.name().equalsIgnoreCase(platform))
            .findAny()
            .orElseThrow(() -> BusTypeNotFoundException.withDetail("platformType: " + platform));
    }
}
