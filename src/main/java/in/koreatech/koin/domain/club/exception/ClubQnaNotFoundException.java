package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ClubQnaNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "동아리 QNA가 존재하지 않습니다.";

    public ClubQnaNotFoundException(String message) {
        super(message);
    }

    public ClubQnaNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubQnaNotFoundException withDetail(String detail) {
        return new ClubQnaNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
