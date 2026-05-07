package in.koreatech.koin.common.model;

import lombok.Getter;

@Getter
public enum MobileAppPath {
    HOME("home"),
    LOGIN("login"),
    SHOP("shop"),
    DINING("dining"),
    KEYWORD("keyword"),
    LOST_ITEM("lost-item"),
    CHAT("chat"),
    CALLVAN("callvan"),
    CALLVAN_CHAT("callvan-chat"),
    CLUB("club"),
    TIMETABLE("timetable"),
    ;

    private final String path;

    MobileAppPath(String path) {
        this.path = path;
    }
}
