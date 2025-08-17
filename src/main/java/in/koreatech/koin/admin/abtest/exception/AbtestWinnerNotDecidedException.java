package in.koreatech.koin.admin.abtest.exception;

import in.koreatech.koin.global.exception.custom.KoinIllegalStateException;

public class AbtestWinnerNotDecidedException extends KoinIllegalStateException {

    private static final String DEFAULT_MESSAGE = "종료되었으나 우승자가 결정되지 않은 AB테스트입니다.";

    public AbtestWinnerNotDecidedException(String message) {
        super(message);
    }

    public AbtestWinnerNotDecidedException(String message, String detail) {
        super(message, detail);
    }

    public static AbtestWinnerNotDecidedException withDetail(String detail) {
        return new AbtestWinnerNotDecidedException(DEFAULT_MESSAGE, detail);
    }
}
