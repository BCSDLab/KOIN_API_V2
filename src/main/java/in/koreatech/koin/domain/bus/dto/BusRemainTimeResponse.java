package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.Clock;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.BusRemainTime;
import in.koreatech.koin.domain.bus.model.city.CityBusRemainTime;
import in.koreatech.koin.domain.bus.model.enums.BusType;

@JsonNaming(SnakeCaseStrategy.class)
public record BusRemainTimeResponse(
    String busType,
    InnerBusResponse nowBus,
    InnerBusResponse nextBus
) {

    public static BusRemainTimeResponse of(BusType busType, List<? extends BusRemainTime> remainTimes, Clock clock) {
        return new BusRemainTimeResponse(
            busType.name().toLowerCase(),
            InnerBusResponse.of(remainTimes, 0, clock),
            InnerBusResponse.of(remainTimes, 1, clock)
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerBusResponse(
        Long busNumber,
        Long remainTime
    ) {

        public static InnerBusResponse of(List<? extends BusRemainTime> remainTimes, int index, Clock clock) {
            if (index < remainTimes.size()) {
                Long busNumber = null;
                Long remainTime = remainTimes.get(index).getRemainSeconds(clock);

                if (remainTime != null && remainTimes.get(index) instanceof CityBusRemainTime cityBusRemainTime) {
                    busNumber = cityBusRemainTime.getBusNumber();
                }

                return new InnerBusResponse(busNumber, remainTime);
            }

            return null;
        }
    }
}
