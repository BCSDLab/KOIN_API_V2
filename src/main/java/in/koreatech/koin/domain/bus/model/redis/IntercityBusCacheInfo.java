package in.koreatech.koin.domain.bus.model.redis;

import java.time.LocalDateTime;
import java.time.LocalTime;

import in.koreatech.koin.domain.bus.model.IntercityBusArrival;
import in.koreatech.koin.domain.bus.model.IntercityBusRoute;

public record IntercityBusCacheInfo (
    IntercityBusRoute intercityBusRoute,
    LocalTime remainTime
) {
    public static IntercityBusCacheInfo from(
        IntercityBusArrival busArrivalInfo,
        LocalDateTime updatedAt
    ) {
        return new IntercityBusCacheInfo(
            IntercityBusRoute.from(busArrivalInfo),
            updatedAt.plusSeconds(busArrivalInfo).toLocalTime()
        );
    }
}
