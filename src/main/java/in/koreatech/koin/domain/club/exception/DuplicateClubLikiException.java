package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin._common.exception.custom.DuplicationException;

public class DuplicateClubLikiException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "이미 좋아요를 누른 동아리입니다!";

    public DuplicateClubLikiException(String message) {
        super(message);
    }

    public DuplicateClubLikiException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicateClubLikiException withDetail(Integer clubId) {
        return new DuplicateClubLikiException(DEFAULT_MESSAGE, "clubId: '" + clubId);
    }
}
