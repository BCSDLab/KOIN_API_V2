package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimeTableFrameResponse(
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = REQUIRED)
    String timetableName,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    boolean isMain
) {
    public static TimeTableFrameResponse from(TimeTableFrame timetableFrame) {
        return new TimeTableFrameResponse(
            timetableFrame.getId(),
            timetableFrame.getName(),
            timetableFrame.isMain()
        );
    }
}
