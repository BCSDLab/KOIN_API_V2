package in.koreatech.koin.domain.bus.model;

import lombok.Getter;

@Getter
public class Bus {
    BusRemainTime remainTime;

    public Bus(BusRemainTime remainTime) {
        this.remainTime = remainTime;
    }

    public static Bus from(BusRemainTime busRemainTime) {
        return new Bus(busRemainTime);
    }
}
