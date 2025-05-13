package in.koreatech.koin.admin.club.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ClubCategoryNotFound extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "동아리 카테고리가 존재하지 않습니다.";

    public ClubCategoryNotFound(String message) {
        super(message);
    }

    public ClubCategoryNotFound(String message, String detail) {
        super(message, detail);
    }

    public static ClubCategoryNotFound withDetail(String detail) {
        return new ClubCategoryNotFound(DEFAULT_MESSAGE, detail);
    }
}
