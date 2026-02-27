package in.koreatech.koin.domain.callvan.model.enums;

import lombok.Getter;

@Getter
public enum CallvanLocation {
    FRONT_GATE("정문"),
    BACK_GATE("후문"),
    TENNIS_COURT("테니스장"),
    DORMITORY_MAIN("본관동"),
    DORMITORY_SUB("별관동"),
    TERMINAL("천안터미널"),
    STATION("천안역"),
    ASAN_STATION("천안아산역"),
    CUSTOM("CUSTOM");

    private final String name;

    CallvanLocation(String name) {
        this.name = name;
    }
}
