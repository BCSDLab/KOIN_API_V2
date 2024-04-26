package in.koreatech.koin.domain.owner.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class OwnerNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "존재하지 않는 사장님입니다.";
    private final String detail;

    public OwnerNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public OwnerNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static OwnerNotFoundException withDetail(String detail) {
        return new OwnerNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return detail;
    }
}
