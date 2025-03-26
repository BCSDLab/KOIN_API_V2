package in.koreatech.koin.domain.user.model;

import lombok.Getter;

@Getter
public enum UserType {
    GENERAL("GENERAL", "일반"),
    STUDENT("STUDENT", "학생"),
    COUNCIL("COUNCIL", "총학생회"),
    COOP("COOP", "영양사"),
    OWNER("OWNER", "사장님"),
    ADMIN("ADMIN", "어드민"),
    ;

    public static final int ANONYMOUS_ID = 0;

    private final String value;
    private final String description;

    UserType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
