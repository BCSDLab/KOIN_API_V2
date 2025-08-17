package in.koreatech.koin.admin.manager.enums;

import lombok.Getter;

@Getter
public enum TrackType {
    ANDROID("Android"),
    BACKEND("Backend"),
    FRONTEND("Frontend"),
    GAME("Game"),
    PM("PM"),
    PL("PL"),
    DESIGN("Design"),
    IOS("iOS"),
    DA("DA"),
    SECURITY("Security")
    ;

    private final String value;

    TrackType(String value) {
        this.value = value;
    }
}
