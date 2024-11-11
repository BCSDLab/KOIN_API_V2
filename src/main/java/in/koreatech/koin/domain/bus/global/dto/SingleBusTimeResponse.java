package in.koreatech.koin.domain.bus.global.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record SingleBusTimeResponse(
    @Schema(description = "버스 타입", example = "shuttle")
    String busName,

    @Schema(description = "도착 시간", example = "12:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime busTime
) {
}
