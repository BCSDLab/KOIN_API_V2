package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ClubEventNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "행사가 존재하지 않습니다.";

    public ClubEventNotFoundException(String message) {
        super(message);
    }

    public ClubEventNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubEventNotFoundException withDetail(String detail) {
        return new ClubEventNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
