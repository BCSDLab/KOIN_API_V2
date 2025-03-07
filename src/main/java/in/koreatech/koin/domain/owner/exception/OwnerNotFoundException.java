package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class OwnerNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 사장님입니다.";

    public OwnerNotFoundException(String message) {
        super(message);
    }

    public OwnerNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static OwnerNotFoundException withDetail(String detail) {
        return new OwnerNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
