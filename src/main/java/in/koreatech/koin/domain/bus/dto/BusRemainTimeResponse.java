package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.Clock;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.BusType;

@JsonNaming(SnakeCaseStrategy.class)
public record BusRemainTimeResponse(
    String busType,
    InnerBusResponse nowBus,
    InnerBusResponse nextBus
) {

    public static BusRemainTimeResponse of(BusType busType, List<BusRemainTime> remainTimes, Clock clock) {
        return new BusRemainTimeResponse(
            busType.getName(),
            InnerBusResponse.of(remainTimes, 0, clock),
            InnerBusResponse.of(remainTimes, 1, clock)
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerBusResponse(
        Long busNumber,
        Long remainTime
    ) {

        public static InnerBusResponse of(List<BusRemainTime> remainTimes, int index, Clock clock) {
            Long result = null;
            if (index < remainTimes.size()) {
                result = remainTimes.get(index).getRemainSeconds(clock);
            }
            return new InnerBusResponse(null, result);
        }
    }
}
