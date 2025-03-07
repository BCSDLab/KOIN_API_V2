package in.koreatech.koin.domain.bus.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ApiTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 API 타입입니다.";

    public ApiTypeNotFoundException(String message) {
        super(message);
    }

    public ApiTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ApiTypeNotFoundException withDetail(String detail) {
        return new ApiTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
