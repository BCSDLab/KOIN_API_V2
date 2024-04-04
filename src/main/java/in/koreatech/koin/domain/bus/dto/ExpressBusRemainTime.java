package in.koreatech.koin.domain.bus.dto;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ExpressBusRemainTime extends BusRemainTime {

    private final String busType;
    private final InnerRemainTime nextBus;
    private final InnerRemainTime nowBus;

    public record InnerRemainTime(
        Long busNumber,
        Long remainTime
    ) {

    }

}
