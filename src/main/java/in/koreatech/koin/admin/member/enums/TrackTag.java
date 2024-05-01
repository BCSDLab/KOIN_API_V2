package in.koreatech.koin.admin.member.enums;

import lombok.Getter;

@Getter
public enum TrackTag {
    ANDROID("Android"),
    BACKEND("BackEnd"),
    FRONTEND("FrontEnd"),
    GAME("Game"),
    PM("P&M"),
    HR("HR"),
    UI_UX("UI/UX"),
    IOS("iOS")
    ;

    private final String tag;

    TrackTag(String tag) {
        this.tag = tag;
    }
}
