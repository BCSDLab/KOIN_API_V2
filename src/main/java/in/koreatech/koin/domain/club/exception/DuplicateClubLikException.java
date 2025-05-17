package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicateClubLikException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 좋아요를 누른 동아리입니다!";

    public DuplicateClubLikException(String message) {
        super(message);
    }

    public DuplicateClubLikException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicateClubLikException withDetail(Integer clubId) {
        return new DuplicateClubLikException(DEFAULT_MESSAGE, "clubId: '" + clubId);
    }
}
