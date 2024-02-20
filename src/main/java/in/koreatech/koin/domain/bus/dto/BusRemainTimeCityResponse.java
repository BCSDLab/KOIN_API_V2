package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.BusCourse;

@JsonNaming(SnakeCaseStrategy.class)
public record BusRemainTimeCityResponse(
    String busType,
    InnerBusResponse nowBus,
    InnerBusResponse nextBus
) {
    public static BusRemainTimeCityResponse from(BusCourse busCourse) {
        return null;
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerBusResponse(
        Long busNumber,
        Long remainTime
    ) {

        public static InnerBusResponse from(BusCourse busCourse) {
            return null;
        }
    }
}
