package in.koreatech.koin.domain.bus.service.model;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import in.koreatech.koin.domain.bus.exception.BusIllegalArrivalTimeException;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class BusRemainTime implements Comparable<BusRemainTime> {

    private final LocalTime busArrivalTime;
    private String busArrivalTimeRaw;

    public boolean isBefore(Clock clock) {
        if (busArrivalTime == null) {
            return false;
        }
        return LocalTime.now(clock).isBefore(busArrivalTime);
    }

    public Long getRemainSeconds(Clock clock) {
        if (isBefore(clock)) {
            return Duration.between(LocalTime.now(clock), busArrivalTime).toSeconds();
        }
        return null;
    }

    public static BusRemainTime from(String arrivalTime) {
        try {
            return builder()
                .busArrivalTime(toLocalTime(arrivalTime))
                .build();
        } catch (BusIllegalArrivalTimeException e) {
            return builder()
                .busArrivalTimeRaw(arrivalTime)
                .build();
        }
    }

    public static BusRemainTime of(Long remainTime, LocalTime updatedAt) {
        return builder()
            .busArrivalTime(updatedAt.plusSeconds(remainTime))
            .build();
    }

    private static LocalTime toLocalTime(String arrivalTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(arrivalTime, formatter);
        } catch (Exception e) {
            throw BusIllegalArrivalTimeException.withDetail("arrivalTime: " + arrivalTime);
        }
    }

    public BusRemainTime(LocalTime busArrivalTime, String busArrivalTimeRaw) {
        this.busArrivalTime = busArrivalTime;
        this.busArrivalTimeRaw = busArrivalTimeRaw;
    }

    @Override
    public int hashCode() {
        return Objects.hash(busArrivalTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BusRemainTime that = (BusRemainTime)o;
        return Objects.equals(busArrivalTime, that.busArrivalTime);
    }

    @Override
    public int compareTo(BusRemainTime o) {
        return busArrivalTime.compareTo(o.busArrivalTime);
    }
}
