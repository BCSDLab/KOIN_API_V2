package in.koreatech.koin.domain.user.model;

import lombok.Getter;

@Getter
public enum UserType {
    STUDENT("STUDENT", "학생"),
    OWNER("OWNER", "사장님"),
    COOP("COOP", "영양사"),
    ;

    public static final Long ANONYMOUS_ID = 0L;

    private final String value;
    private final String description;

    UserType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
