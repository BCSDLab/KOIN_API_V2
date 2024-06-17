package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimetableFrameUpdateResponse(
    @Schema(description = "학기 정보", example = "20192", requiredMode = REQUIRED)
    String semester,

    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = REQUIRED)
    String name,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    boolean isMain
) {

    public static TimetableFrameUpdateResponse from(TimetableFrame timeTableFrame) {
        return new TimetableFrameUpdateResponse(
            timeTableFrame.getSemester().getSemester(),
            timeTableFrame.getName(),
            timeTableFrame.isMain());
    }
}
