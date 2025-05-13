package in.koreatech.koin.admin.club.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ClubNotFound extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "동아리가 존재하지 않습니다.";

    public ClubNotFound(String message) {
        super(message);
    }

    public ClubNotFound(String message, String detail) {
        super(message, detail);
    }

    public static ClubNotFound withDetail(String detail) {
        return new ClubNotFound(DEFAULT_MESSAGE, detail);
    }
}
