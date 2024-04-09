package in.koreatech.koin.domain.bus.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record SingleBusTimeResponse(
    String busName,
    LocalTime busTime
) {
}
