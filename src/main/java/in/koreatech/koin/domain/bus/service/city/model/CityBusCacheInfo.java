package in.koreatech.koin.domain.bus.service.city.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

import in.koreatech.koin.domain.bus.service.model.BusRemainTime;

public record CityBusCacheInfo(
    Long busNumber,
    LocalTime remainTime
) {

    /**
     * <pre>
     * 남은 시간 = (캐시 저장 시각 + 저장된 남은시간) - 현재시각
     * {@link BusRemainTime#getRemainSeconds}에서는 남은 시간을 (저장된 남은시간 - 현재시각)으로 계산중
     * 학교 버스는 도착 시간을 저장하고, 시내버스는 남은 시간만을 저장하므로
     * Redis에 저장할때 (캐시 저장 시각 + 남은시간)으로 저장하여 통일시켜줌</pre>
     * */
    public static CityBusCacheInfo of(CityBusArrival busArrivalInfo, LocalDateTime updatedAt) {
        return new CityBusCacheInfo(
            busArrivalInfo.routeno(),
            updatedAt.plusSeconds(busArrivalInfo.arrtime()).toLocalTime()
        );
    }
}
