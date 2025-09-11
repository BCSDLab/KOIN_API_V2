package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ClubLikeNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "좋아요를 누른 적 없는 동아리입니다!";

    public ClubLikeNotFoundException(String message) {
        super(message);
    }

    public ClubLikeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubLikeNotFoundException withDetail(Integer clubId) {
        return new ClubLikeNotFoundException(DEFAULT_MESSAGE, "clubId: " + clubId);
    }
}
