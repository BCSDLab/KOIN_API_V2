package in.koreatech.koin.global.fcm;

import lombok.Getter;

@Getter
public enum MobileAppPath {
    HOME("home"),
    LOGIN("login"),
    SHOP("shop"),
    DINING("dining"),
    KEYWORD("keyword")
    ;

    private final String path;

    MobileAppPath(String path) {
        this.path = path;
    }
}
