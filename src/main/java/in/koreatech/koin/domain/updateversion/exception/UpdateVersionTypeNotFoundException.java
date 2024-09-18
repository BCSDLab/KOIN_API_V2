package in.koreatech.koin.domain.updateversion.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class UpdateVersionTypeNotFoundException extends DataNotFoundException {

    public static final String DEFAULT_MESSAGE = "존재하지 않는 업데이트 버전 타입입니다.";

    public UpdateVersionTypeNotFoundException(String message) {
        super(message);
    }

    public UpdateVersionTypeNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static UpdateVersionTypeNotFoundException withDetail(String detail) {
        return new UpdateVersionTypeNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
