package in.koreatech.koin.domain.bus.shuttle.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.shuttle.model.BusCourse;
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

    public static BusCourseResponse from(BusCourse busCourse) {
        return new BusCourseResponse(
            busCourse.getBusType(),
            busCourse.getDirection(),
            busCourse.getRegion()
        );
    }
}
