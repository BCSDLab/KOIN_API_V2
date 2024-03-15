package in.koreatech.koin.domain.dining.exception;

import java.time.format.DateTimeParseException;

public class InvalidFormatException extends DateTimeParseException {
    private static final String DEFAULT_MESSAGE = "올바른 날짜 형식(yyMMdd)이 아닙니다.";

    public InvalidFormatException(String message, CharSequence parsedData, int errorIndex) {
        super(message, parsedData, errorIndex);
    }

    public static InvalidFormatException withDetail(String detail, CharSequence parsedData, int errorIndex) {
        String message = String.format("%s %s", DEFAULT_MESSAGE, detail);
        return new InvalidFormatException(message, parsedData, errorIndex);
    }
}
