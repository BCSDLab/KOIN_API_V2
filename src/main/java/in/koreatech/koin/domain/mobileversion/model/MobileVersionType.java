package in.koreatech.koin.domain.mobileversion.model;

import java.util.Arrays;

import in.koreatech.koin.domain.version.exception.VersionTypeNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MobileVersionType {
    ANDROID("android"),
    IOS("ios")
    ;

    private final String value;

    public static MobileVersionType from(String value) {
        return Arrays.stream(values())
            .filter(versionType -> versionType.value.equals(value))
            .findAny()
            .orElseThrow(() -> VersionTypeNotFoundException.withDetail("versionType: " + value));
    }
}

