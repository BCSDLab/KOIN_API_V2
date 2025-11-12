package in.koreatech.koin.global.exception;

import org.springframework.util.StringUtils;

import in.koreatech.koin.global.code.ApiResponseCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ApiResponseCode errorCode;
    private final String detail;

    private CustomException(ApiResponseCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    private CustomException(ApiResponseCode errorCode, String detail, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public static CustomException of(ApiResponseCode errorCode) {
        return new CustomException(errorCode, "");
    }

    public static CustomException of(ApiResponseCode errorCode, String detail) {
        return new CustomException(errorCode, detail);
    }

    public static CustomException of(ApiResponseCode errorCode, String detail, Throwable cause) {
        return new CustomException(errorCode, detail, cause);
    }

    public String getFullMessage() {
        if (StringUtils.hasText(detail)) {
            return super.getMessage();
        }
        return String.format("%s: %s", getMessage(), detail);
    }
}
