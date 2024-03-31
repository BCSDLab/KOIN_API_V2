package in.koreatech.koin.global.date;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final ZoneId KST_TIMEZONE = ZoneId.of("Asia/Seoul");

    public static Integer getYearOfNow() {
        return LocalDateTime.ofInstant(Instant.now(), KST_TIMEZONE).getYear();
    }

    public static Long getTimeStampSecondOfNow() {
        return Instant.now().getEpochSecond();
    }

    public static Date addHoursToJavaUtilDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    public static Date addMinute(Date date, int amount) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, amount);

        return calendar.getTime();
    }

    public static Boolean isExpired(Date origin, Date candidate) {
        return origin.compareTo(candidate) < 0;
    }
}
