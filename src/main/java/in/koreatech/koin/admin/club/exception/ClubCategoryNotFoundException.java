package in.koreatech.koin.admin.club.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ClubCategoryNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "동아리 카테고리가 존재하지 않습니다.";

    public ClubCategoryNotFoundException(String message) {
        super(message);
    }

    public ClubCategoryNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubCategoryNotFoundException withDetail(String detail) {
        return new ClubCategoryNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
