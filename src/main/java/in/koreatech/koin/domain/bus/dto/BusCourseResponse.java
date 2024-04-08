package in.koreatech.koin.domain.bus.dto;

import in.koreatech.koin.domain.bus.model.mongo.BusCourse;

public record BusCourseResponse(
    String busType,
    String direction,
    String region

) {

    public static BusCourseResponse from(BusCourse busCourse){
        return new BusCourseResponse(
            busCourse.getBusType(),
            busCourse.getDirection(),
            busCourse.getRegion()
        );
    }
}
