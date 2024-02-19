package in.koreatech.koin.domain.bus.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;

public class BusRemainTime {

    private final LocalTime busArrivalTime;

    public boolean isBefore() {
        return LocalTime.now().isBefore(busArrivalTime);
    }

    public Long getRemainSeconds() {
        if (isBefore()) {
            return Duration.between(LocalTime.now(), busArrivalTime).toSeconds();
        }
        return null;
        // return 86400L - Duration.between(busArrivalTime, LocalTime.now()).toSeconds();
    }

    public static BusRemainTime from(String remainTime) {
        return builder()
            .busArrivalTime(toLocalTime(remainTime))
            .build();
    }

    private static LocalTime toLocalTime(String remainTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(remainTime, formatter);
    }

    @Builder
    private BusRemainTime(LocalTime busArrivalTime) {
        this.busArrivalTime = busArrivalTime;
    }
}
