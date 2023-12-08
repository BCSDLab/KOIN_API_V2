package in.koreatech.koin.domain.user;

import lombok.Getter;

@Getter
public enum UserType {
    STUDENT("학생"),
    USER("사용자"),
    ;

    private final String value;

    UserType(String value) {
        this.value = value;
    }
}
