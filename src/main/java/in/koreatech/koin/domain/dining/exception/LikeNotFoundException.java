package in.koreatech.koin.domain.dining.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class LikeNotFoundException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "좋아요를 누른적이 없는 식단입니다!";

    public LikeNotFoundException(String message) {
        super(message);
    }

    public LikeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static LikeNotFoundException withDetail(Integer diningId, Integer userId) {
        return new LikeNotFoundException(DEFAULT_MESSAGE, "diningId: '" + diningId + "'" + "userId: " + userId);
    }
}
