package in.koreatech.koin.admin.manager.enums;

import lombok.Getter;

@Getter
public enum TeamType {
    KOIN("Koin"),
    BUSINESS("Business"),
    CAMPUS("Campus"),
    USER("User"),
    ;

    private final String value;

    TeamType(String value) {
        this.value = value;
    }
}
