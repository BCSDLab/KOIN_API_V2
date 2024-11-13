package in.koreatech.koin.domain.bus.city.model;

import java.time.LocalTime;

public record CityBusCacheInfo(
    Long busNumber,
    LocalTime remainTime
) {
}
