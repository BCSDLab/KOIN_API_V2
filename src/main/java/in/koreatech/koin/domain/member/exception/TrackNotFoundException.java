package in.koreatech.koin.domain.member.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class TrackNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 트랙입니다.";

    public TrackNotFoundException(String message) {
        super(message);
    }

    public TrackNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static TrackNotFoundException withDetail(String detail) {
        return new TrackNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
