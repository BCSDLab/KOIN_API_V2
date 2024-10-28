package in.koreatech.koin.admin.user.enums;

import java.util.Arrays;

import in.koreatech.koin.admin.user.exception.AdminTrackNotValidException;
import lombok.Getter;

@Getter
public enum TrackType {
    ANDROID("안드로이드"),
    BACKEND("백엔드"),
    FRONTEND("프론트엔드"),
    GAME("게임"),
    PM("프로덕트 매니저"),
    DESIGN("디자인"),
    IOS("아이오에스"),
    DA("데이터 분석"),
    ;

    private final String value;

    TrackType(String value) {
        this.value = value;
    }

    public static void checkTrackValid(String trackName) {
        boolean isValid = Arrays.stream(TrackType.values())
            .anyMatch(trackType -> trackType.getValue().equals(trackName));

        if (!isValid) {
            throw AdminTrackNotValidException.withDetail("trackName : " + trackName);
        }
    }
}
