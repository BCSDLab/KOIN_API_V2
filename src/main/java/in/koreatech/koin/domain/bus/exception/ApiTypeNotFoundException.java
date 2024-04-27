package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class ApiTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 API 타입입니다.";
    private final String detail;

    public ApiTypeNotFoundException(String message) {
        super(message);
        this.detail = null;
    }

    public ApiTypeNotFoundException(String message, String detail) {
        super(message);
        this.detail = detail;
    }

    public static ApiTypeNotFoundException withDetail(String detail) {
        return new ApiTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }

    @Override
    public String getDetail() {
        return DEFAULT_MESSAGE;
    }
}
