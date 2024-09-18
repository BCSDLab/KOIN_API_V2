package in.koreatech.koin.domain.updateversion.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.updateversion.exception.UpdateVersionTypeNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UpdateVersionType {
    ANDROID("android"),
    IOS("ios");

    private final String value;

    @JsonCreator
    public static UpdateVersionType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> UpdateVersionTypeNotFoundException.withDetail("versionType: " + value));
    }
}

