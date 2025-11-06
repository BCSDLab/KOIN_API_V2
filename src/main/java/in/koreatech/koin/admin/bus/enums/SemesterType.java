package in.koreatech.koin.admin.bus.enums;

public enum SemesterType {
    SEASONAL("계절학기"),
    VACATION("정규학기"),
    REGULAR("방학"),
    ;

    private final String description
    ;

    SemesterType(String description) {
        this.description = description;
    }
}
