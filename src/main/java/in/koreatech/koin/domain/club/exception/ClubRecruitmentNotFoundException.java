package in.koreatech.koin.domain.club.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ClubRecruitmentNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "동아리 모집 공고가 존재하지 않습니다.";

    public ClubRecruitmentNotFoundException(String message) {
        super(message);
    }

    public ClubRecruitmentNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ClubRecruitmentNotFoundException withDetail(String detail) {
        return new ClubRecruitmentNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
