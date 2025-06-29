package in.koreatech.koin._common.exception;

import java.util.Objects;

import in.koreatech.koin._common.code.ApiResponseCode;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;

public class CustomException extends RuntimeException {

    @Getter
    private final ApiResponseCode userErrorCode;
    private final String detail;

    private CustomException(ApiResponseCode userErrorCode, String detail) {
        super(userErrorCode.getMessage());
        this.userErrorCode = userErrorCode;
        this.detail = detail;
    }

    public static CustomException of(ApiResponseCode userErrorCode) {
        return new CustomException(userErrorCode, "");
    }

    public static CustomException of(ApiResponseCode userErrorCode, Object errorObject) {
        return new CustomException(userErrorCode, Objects.toString(errorObject, ""));
    }

    public String getFullMessage() {
        if (StringUtils.isBlank(detail)) {
            return getMessage();
        }
        return String.format("%s: %s", getMessage(), detail);
    }
}
