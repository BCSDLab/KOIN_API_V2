package in.koreatech.koin.domain.graduation.exception;

import in.koreatech.koin.global.exception.custom.DataNotFoundException;

public class ExcelFileCheckException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "엑셀 파일 형식이 아닙니다.";

    public ExcelFileCheckException(String message) {
        super(message);
    }

    public ExcelFileCheckException(String message, String detail) {
        super(message, detail);
    }

    public static ExcelFileCheckException withDetail(String detail) {
        return new ExcelFileCheckException(DEFAULT_MESSAGE, detail);
    }
}
