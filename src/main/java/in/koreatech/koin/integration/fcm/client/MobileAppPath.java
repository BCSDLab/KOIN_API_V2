package in.koreatech.koin.integration.fcm.client;

import lombok.Getter;

@Getter
public enum MobileAppPath {
    HOME("home"),
    LOGIN("login"),
    SHOP("shop"),
    DINING("dining"),
    KEYWORD("keyword"),
    CHAT("chat")
    ;

    private final String path;

    MobileAppPath(String path) {
        this.path = path;
    }
}
