package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ClubNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "동아리가 존재하지 않습니다.";

    public ClubNotFoundException(String message) {
        super(message);
    }

    public ClubNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubNotFoundException withDetail(String detail) {
        return new ClubNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
