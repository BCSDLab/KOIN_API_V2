package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;

public class AbtestTitleIllegalArgumentException extends KoinIllegalArgumentException {

    private static final String DEFAULT_MESSAGE = "실험 이름이 잘못되었습니다.";

    public AbtestTitleIllegalArgumentException(String message) {
        super(message);
    }

    public AbtestTitleIllegalArgumentException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestTitleIllegalArgumentException withDetail(String detail) {
        return new AbtestTitleIllegalArgumentException(DEFAULT_MESSAGE, detail);
    }
}
