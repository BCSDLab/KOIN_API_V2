package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.BusRemainTime;

@JsonNaming(SnakeCaseStrategy.class)
public record BusRemainTimeResponse(
    String busType,
    InnerBusResponse nowBus,
    InnerBusResponse nextBus
) {

    public static BusRemainTimeResponse of(String busType, List<BusRemainTime> remainTimes) {
        return new BusRemainTimeResponse(
            busType,
            InnerBusResponse.from(remainTimes, 0),
            InnerBusResponse.from(remainTimes, 1)
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerBusResponse(
        Long remainTime
    ) {

        public static InnerBusResponse from(List<BusRemainTime> remainTimes, int index) {
            Long result = null;
            if (index < remainTimes.size()) {
                result = remainTimes.get(index).getRemainSeconds();
            }
            return new InnerBusResponse(result);
        }
    }
}
