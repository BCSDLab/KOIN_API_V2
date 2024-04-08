package in.koreatech.koin.global.fcm;

import lombok.Getter;

@Getter
public enum MobileAppPath {
    HOME("home", "home"),
    LOGIN("login", "login"),
    ;

    private final String android;
    private final String apple;

    MobileAppPath(String android, String apple) {
        this.android = android;
        this.apple = apple;
    }
}
