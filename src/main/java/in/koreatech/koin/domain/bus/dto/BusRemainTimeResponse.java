package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.time.Clock;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.service.model.BusRemainTime;
import in.koreatech.koin.domain.bus.service.city.model.CityBusRemainTime;
import in.koreatech.koin.domain.bus.enums.BusType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record BusRemainTimeResponse(
    @Schema(description = "버스 타입", example = "shuttle", requiredMode = NOT_REQUIRED)
    String busType,
    InnerBusResponse nowBus,
    InnerBusResponse nextBus
) {

    public static BusRemainTimeResponse of(BusType busType, List<? extends BusRemainTime> remainTimes, Clock clock) {
        return new BusRemainTimeResponse(
            busType.getName(),
            InnerBusResponse.of(remainTimes, 0, clock),
            InnerBusResponse.of(remainTimes, 1, clock)
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerBusResponse(
        @Schema(description = "버스 번호", example = "400", requiredMode = NOT_REQUIRED)
        Long busNumber,

        @Schema(description = "남은 시간 / 초", example = "10417", requiredMode = NOT_REQUIRED)
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
