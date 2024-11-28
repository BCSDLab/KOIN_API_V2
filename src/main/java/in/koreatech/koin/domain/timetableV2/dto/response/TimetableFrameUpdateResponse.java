package in.koreatech.koin.domain.timetableV2.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableFrameUpdateResponse(
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = REQUIRED)
    String timetableName,

    @Schema(description = "메인 시간표 여부", example = "false", requiredMode = REQUIRED)
    Boolean isMain
) {
    public static TimetableFrameUpdateResponse from(TimetableFrame timetableFrame) {
        return new TimetableFrameUpdateResponse(
            timetableFrame.getId(),
            timetableFrame.getName(),
            timetableFrame.isMain()
        );
    }
}
