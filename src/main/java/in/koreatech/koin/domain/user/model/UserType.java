package in.koreatech.koin.domain.user.model;

import lombok.Getter;

@Getter
public enum UserType {
    STUDENT("STUDENT", "학생"),
    USER("USER", "사용자"),
    OWNER("OWNER", "사장님"),
    ;

    private final String value;
    private final String description;

    UserType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
