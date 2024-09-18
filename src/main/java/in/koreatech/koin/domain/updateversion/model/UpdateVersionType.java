package in.koreatech.koin.domain.updateversion.model;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import in.koreatech.koin.domain.updateversion.exception.UpdateVersionTypeNotFoundException;
import lombok.Getter;

@Getter
public enum UpdateVersionType {
    ANDROID("android"),
    IOS("ios");

    private final String value;

    UpdateVersionType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static UpdateVersionType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.value.equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(() -> UpdateVersionTypeNotFoundException.withDetail("versionType: " + value));
    }
}

