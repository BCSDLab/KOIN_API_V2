package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableFrameResponse(
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = REQUIRED)
    String timetableName,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    Boolean isMain
) {
    public static TimetableFrameResponse from(TimetableFrame timetableFrame) {
        return new TimetableFrameResponse(
            timetableFrame.getId(),
            timetableFrame.getName(),
            timetableFrame.getIsMain()
        );
    }
}
