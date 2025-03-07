package in.koreatech.koin._common.util.datetime;

import java.time.LocalDate;

public class DateTimes {

    private DateTimes() {
    }

    public static boolean isBetween(LocalDate start, LocalDate end, LocalDate target) {
        return isAfterEqualsTo(target, start) && isBeforeEqualsTo(target, end);
    }

    public static boolean isAfterEqualsTo(LocalDate date, LocalDate standardDate) {
        return !date.isBefore(standardDate);
    }

    public static boolean isBeforeEqualsTo(LocalDate date, LocalDate standardDate) {
        return !date.isAfter(standardDate);
    }
}
