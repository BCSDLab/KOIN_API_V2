package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.timetable.model.TimetablesFrame;
import io.swagger.v3.oas.annotations.media.Schema;

public record TimetablesFrameResponse (
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = REQUIRED)
    String timetableName
) {
    public static TimetablesFrameResponse from(TimetablesFrame timetablesFrame) {
        return new TimetablesFrameResponse(
            timetablesFrame.getId(),
            timetablesFrame.getName()
        );
    }
}
