package in.koreatech.koin.common.model;

import lombok.Getter;

@Getter
public enum MobileAppPath {
    HOME("home"),
    LOGIN("login"),
    SHOP("shop"),
    DINING("dining"),
    KEYWORD("keyword"),
    CHAT("chat"),
    CLUB("club"),
    TIMETABLE("timetable"),
    ;

    private final String path;

    MobileAppPath(String path) {
        this.path = path;
    }
}
