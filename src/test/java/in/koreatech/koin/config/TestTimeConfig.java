package in.koreatech.koin.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;

@TestConfiguration
public class TestTimeConfig {

    private final LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 15, 12, 0);
    private LocalDateTime currTime = fixedTime;

    public void setCurrTime(LocalDateTime localDateTime) {
        this.currTime = localDateTime;
    }

    public void setOriginTime() {
        this.currTime = fixedTime;
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(currTime);
    }

    @Bean
    public Clock clock() {
        return Clock.fixed(
            currTime.atZone(Clock.systemDefaultZone().getZone()).toInstant(),
            Clock.systemDefaultZone().getZone()
        );
    }
}
