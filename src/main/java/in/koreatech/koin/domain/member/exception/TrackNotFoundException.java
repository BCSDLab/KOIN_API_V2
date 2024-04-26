package in.koreatech.koin.domain.member.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class TrackNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 트랙입니다.";
    private final String detail;

    public TrackNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public TrackNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static TrackNotFoundException withDetail(String detail) {
        return new TrackNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
