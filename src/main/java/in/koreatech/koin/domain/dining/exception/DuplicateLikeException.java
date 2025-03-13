package in.koreatech.koin.domain.dining.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicateLikeException extends DuplicationException {
    private static final String DEFAULT_MESSAGE = "이미 좋아요를 누른 식단입니다!";

    public DuplicateLikeException(String message) {
        super(message);
    }

    public DuplicateLikeException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicateLikeException withDetail(Integer diningId, Integer userId) {
        return new DuplicateLikeException(DEFAULT_MESSAGE, "diningId: '" + diningId + "'" + "userId: " + userId);
    }
}
