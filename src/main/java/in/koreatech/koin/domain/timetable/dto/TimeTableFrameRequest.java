package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TimeTableFrameRequest(
    @Schema(description = "학기 정보", example = "20192", requiredMode = REQUIRED)
    @NotBlank(message = "학기 정보를 입력해주세요")
    String semester
) {
    public TimeTableFrame toTimetablesFrame(User user, Semester semester, String name, boolean isMain) {
        return TimeTableFrame.builder()
            .user(user)
            .semester(semester)
            .name(name)
            .isMain(isMain)
            .build();
    }
}