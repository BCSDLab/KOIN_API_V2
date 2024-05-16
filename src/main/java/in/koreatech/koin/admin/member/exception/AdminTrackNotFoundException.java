package in.koreatech.koin.admin.member.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class AdminTrackNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 트랙입니다.";

    protected AdminTrackNotFoundException(String message) {
        super(message);
    }

    protected AdminTrackNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static AdminTrackNotFoundException withDetail(String detail) {
        return new AdminTrackNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
