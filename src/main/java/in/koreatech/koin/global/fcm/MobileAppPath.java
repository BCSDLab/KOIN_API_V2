package in.koreatech.koin.global.fcm;

import lombok.Getter;

@Getter
public enum MobileAppPath {
    HOME("home", "home"),
    LOGIN("login", "login"),
    SHOP("shop", "shop"),
    DINING("dining", "dining"),
    KEYWORD("keyword", "keyword")
    ;

    private final String android;
    private final String apple;

    MobileAppPath(String android, String apple) {
        this.android = android;
        this.apple = apple;
    }
}
