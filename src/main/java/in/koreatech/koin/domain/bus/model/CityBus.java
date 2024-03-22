package in.koreatech.koin.domain.bus.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CityBus extends Bus {
    private final Long busNumber;

    @Builder
    private CityBus(Long busNumber, BusRemainTime busRemainTime) {
        super(busRemainTime);
        this.busNumber = busNumber;
    }

    public static CityBus from(CityBusArrivalInfo busArrivalInfo) {
        return CityBus.builder()
            .busNumber(busArrivalInfo.routeno())
            .busRemainTime(BusRemainTime.from(busArrivalInfo.arrtime()))
            .build();
    }
}
