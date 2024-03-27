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

    public static CityBus from(BusCache busInfo) {
        return CityBus.builder()
            .busNumber(busInfo.busNumber())
            .busRemainTime(
                BusRemainTime.builder()
                    .busArrivalTime(busInfo.remainTime())
                    .build()
            )
            .build();
    }
}
