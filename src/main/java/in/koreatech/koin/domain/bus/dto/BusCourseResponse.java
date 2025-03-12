package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.enums.BusDirection;
import in.koreatech.koin.domain.bus.enums.ShuttleRouteName;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BusCourseResponse(
    @Schema(description = "버스 타입", example = "shuttle", requiredMode = NOT_REQUIRED)
    String busType,

    @Schema(description = "방향", example = "to", requiredMode = NOT_REQUIRED)
    String direction,

    @Schema(description = "기준", example = "청주", requiredMode = NOT_REQUIRED)
    String region
) {

    public static BusCourseResponse of(ShuttleRouteName routeName, BusDirection direction) {
        return new BusCourseResponse(
            routeName.getBusType().toString().toLowerCase(),
            direction.getLegacyDirection(),
            routeName.getLabel()
        );
    }
}
