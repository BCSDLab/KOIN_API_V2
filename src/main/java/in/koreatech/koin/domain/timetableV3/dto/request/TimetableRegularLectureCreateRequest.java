package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableRegularLectureCreateRequest(
    @NotNull(message = "시간표 프레임 id를 입력해주세요.")
    @Schema(description = "시간표 프레임 id", example = "1004", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @NotNull(message = "정규 강의 id를 입력해주세요.")
    @Schema(description = "정규 강의 id", example = "3015", requiredMode = REQUIRED)
    Integer lectureId
) {
    public TimetableLecture toTimetableLecture(
        TimetableFrame frame, Lecture lecture, CourseType courseType, GeneralEducationArea generalEducationArea
    ) {
        return TimetableLecture.builder()
            .lecture(lecture)
            .timetableFrame(frame)
            .grades("0")
            .courseType(courseType)
            .generalEducationArea(generalEducationArea)
            .build();
    }
}
