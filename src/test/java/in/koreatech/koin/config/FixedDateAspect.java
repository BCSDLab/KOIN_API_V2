package in.koreatech.koin.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FixedDateAspect {

    @Autowired
    private TestTimeConfig testTimeConfig;

    @Around("@annotation(fixedDate)")
    public Object around(ProceedingJoinPoint joinPoint, FixedDate fixedDate) throws Throwable {
        LocalDate date = LocalDate.of(fixedDate.year(), fixedDate.month(), fixedDate.day());
        LocalDateTime fixedDateTime = date.atStartOfDay();
        testTimeConfig.setCurrTime(fixedDateTime);
        try {
            return joinPoint.proceed();
        } finally {
            testTimeConfig.setOriginTime();
        }
    }
}
