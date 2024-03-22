package in.koreatech.koin.domain.bus.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record BusInfoCache(
    Long busNumber,
    LocalTime remainTime
) {

    public static BusInfoCache from(CityBusArrivalInfo busArrivalInfo, LocalDateTime updatedAt) {
        return new BusInfoCache(
            busArrivalInfo.routeno(),
            updatedAt.plusSeconds(busArrivalInfo.arrtime()).toLocalTime()
        );
    }

}
