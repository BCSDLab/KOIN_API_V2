package in.koreatech.koin.domain.bus.dto;

import java.time.LocalTime;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import lombok.Getter;

@Getter
public class ExpressBusRemainTime extends BusRemainTime {

    private final String busType;

    public ExpressBusRemainTime(LocalTime busArrivalTime, String busType) {
        super(busArrivalTime);
        this.busType = busType;
    }
}
