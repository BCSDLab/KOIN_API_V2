package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class OwnerNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 사장님입니다.";

    public OwnerNotFoundException(String message) {
        super(message);
    }

    public static OwnerNotFoundException withDetail(String detail) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new OwnerNotFoundException(message);
    }
}
