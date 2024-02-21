package in.koreatech.koin.domain.bus.model;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class BusRemainTime implements Comparable<BusRemainTime> {

    private final LocalTime busArrivalTime;

    public boolean isBefore() {
        return LocalTime.now().isBefore(busArrivalTime);
    }

    public Long getRemainSeconds() {
        if (isBefore()) {
            return Duration.between(LocalTime.now(), busArrivalTime).toSeconds();
        }
        return null;
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

    @Override
    public int compareTo(BusRemainTime o) {
        return busArrivalTime.compareTo(o.busArrivalTime);
    }
}
