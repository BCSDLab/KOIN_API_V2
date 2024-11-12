package in.koreatech.koin.domain.timetableV2.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(SnakeCaseStrategy.class)
public record TimetableFrameCreateRequest(
    @Schema(description = "학기 정보", example = "20192", requiredMode = REQUIRED)
    @NotBlank(message = "학기 정보를 입력해주세요")
    String semester,

    @Schema(description = "시간표 이름", example = "시간표1", requiredMode = NOT_REQUIRED)
    String timetableName
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
