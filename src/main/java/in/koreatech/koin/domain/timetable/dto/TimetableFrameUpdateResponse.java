package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimetableFrameUpdateResponse(
    @Schema(description = "학기 정보", example = "20192", requiredMode = REQUIRED)
    String semester,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    String name,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    boolean isMain
) {

    public static TimetableFrameUpdateResponse from(TimeTableFrame timeTableFrame) {
        return new TimetableFrameUpdateResponse(
            timeTableFrame.getSemester().getSemester(),
            timeTableFrame.getName(),
            timeTableFrame.isMain());
    }
}
