package in.koreatech.koin.domain.user.model;

import lombok.Getter;

@Getter
public enum UserType {
    STUDENT("STUDENT", "학생"),
    OWNER("OWNER", "사장님"),
    COOP("COOP", "영양사"),
    ADMIN("ADMIN", "어드민");

    public static final int ANONYMOUS_ID = 0;

    private final String value;
    private final String description;

    UserType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
