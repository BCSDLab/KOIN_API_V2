package in.koreatech.koin.domain.bus.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.bus.model.mongo.BusCourse;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BusCourseResponse(
    String busType,
    String direction,
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
