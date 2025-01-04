package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableFrameCreateRequestV3(
    @Schema(description = "학기 년도", example = "2019", requiredMode = REQUIRED)
    @NotNull(message = "학기 년도를 입력해주세요")
    Integer year,

    @Schema(description = "년도", example = "2학기", requiredMode = REQUIRED)
    @NotNull(message = "학기를 입력해주세요")
    String term
) {
    public TimetableFrame toTimetablesFrame(User user, Semester semester, String name, boolean isMain) {
        return TimetableFrame.builder()
            .user(user)
            .semester(semester)
            .name(name)
            .isMain(isMain)
            .build();
    }
}
