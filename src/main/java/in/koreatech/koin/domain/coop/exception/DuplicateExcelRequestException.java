package in.koreatech.koin.domain.coop.exception;

import java.time.LocalDate;

import in.koreatech.koin.global.exception.custom.DuplicationException;

public class DuplicateExcelRequestException extends DuplicationException {

    private static final String DEFAULT_MESSAGE = "동일한 요청을 30초 안에 다시 보낼 수 없습니다!";

    public DuplicateExcelRequestException(String message) {
        super(message);
    }

    public DuplicateExcelRequestException(String message, String detail) {
        super(message, detail);
    }

    public static DuplicateExcelRequestException withDetail(LocalDate startDate, LocalDate endDate) {
        return new DuplicateExcelRequestException(DEFAULT_MESSAGE,
            "startDate: '" + startDate + "'" + "endDate: " + endDate);
    }
}
