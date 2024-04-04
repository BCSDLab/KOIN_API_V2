package in.koreatech.koin.domain.bus.model.city;

import java.time.LocalTime;
import java.util.Objects;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CityBusRemainTime extends BusRemainTime {
    private final Long busNumber;

    public CityBusRemainTime(Long busNumber, LocalTime busArrivalTime) {
        super(busArrivalTime);
        this.busNumber = busNumber;
    }

    public static CityBusRemainTime from(CityBusCacheInfo busInfo) {
        return builder()
            .busNumber(busInfo.busNumber())
            .busArrivalTime(busInfo.remainTime())
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBusArrivalTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CityBusRemainTime that = (CityBusRemainTime)o;
        return Objects.equals(getBusArrivalTime(), that.getBusArrivalTime())
            && Objects.equals(busNumber, that.busNumber);
    }

    @Override
    public int compareTo(BusRemainTime o) {
        return getBusArrivalTime().compareTo(o.getBusArrivalTime());
    }
}
