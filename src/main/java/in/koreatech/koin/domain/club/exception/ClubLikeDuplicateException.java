package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class ClubLikeDuplicateException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 좋아요를 누른 동아리입니다!";

    public ClubLikeDuplicateException(String message) {
        super(message);
    }

    public ClubLikeDuplicateException(String message, String detail) {
        super(message, detail);
    }

    public static ClubLikeDuplicateException withDetail(Integer clubId) {
        return new ClubLikeDuplicateException(DEFAULT_MESSAGE, "clubId: '" + clubId);
    }
}
