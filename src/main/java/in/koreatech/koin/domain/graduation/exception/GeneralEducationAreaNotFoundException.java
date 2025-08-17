package in.koreatech.koin.domain.graduation.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class GeneralEducationAreaNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 교양 이수구분입니다.";

    protected GeneralEducationAreaNotFoundException(String message) {
        super(message);
    }

    protected GeneralEducationAreaNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static GeneralEducationAreaNotFoundException withDetail(String detail) {
        return new GeneralEducationAreaNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
