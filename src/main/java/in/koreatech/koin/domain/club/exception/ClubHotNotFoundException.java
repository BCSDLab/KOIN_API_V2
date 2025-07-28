package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ClubHotNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "인기 동아리가 존재하지 않습니다.";

    public ClubHotNotFoundException(String message) {
        super(message);
    }

    public ClubHotNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubHotNotFoundException withDetail(String detail) {
        return new ClubHotNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
