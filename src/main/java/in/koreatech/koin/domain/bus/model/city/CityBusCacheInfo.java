package in.koreatech.koin.domain.bus.model.city;

import java.time.LocalTime;

public record CityBusCacheInfo(
    Long busNumber,
    LocalTime remainTime
) {
}
