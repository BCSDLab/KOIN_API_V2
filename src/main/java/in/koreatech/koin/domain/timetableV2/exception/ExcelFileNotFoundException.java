package in.koreatech.koin.domain.timetableV2.exception;

import in.koreatech.koin._common.exception.custom.DataNotFoundException;

public class ExcelFileNotFoundException extends DataNotFoundException {
    private static final String DEFAULT_MESSAGE = "엑셀 파일을 찾을 수 없습니다.";

    public ExcelFileNotFoundException(String message) {
        super(message);
    }

    public ExcelFileNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static ExcelFileNotFoundException withDetail(String detail) {
        return new ExcelFileNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
