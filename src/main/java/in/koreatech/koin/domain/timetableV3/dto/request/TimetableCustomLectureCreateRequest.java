package in.koreatech.koin.domain.timetableV3.dto.request;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.timetableV3.utils.ClassPlaceUtils.joinClassPlaces;
import static in.koreatech.koin.domain.timetableV3.utils.ClassTimeUtils.joinClassTimes;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableCustomLectureCreateRequest(
    @NotNull(message = "시간표 프레임 id를 입력해주세요.")
    @Schema(description = "시간표 프레임 id", example = "1004", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @Valid
    @Schema(description = "커스텀 강의 정보", requiredMode = REQUIRED)
    @NotNull(message = "커스텀 강의 정보를 입력해주세요.")
    InnerTimeTableCustomLectureRequest timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableCustomLectureRequest(
        @Schema(description = "커스텀 강의 이름", example = "커스텀 강의 이름", requiredMode = NOT_REQUIRED)
        @Size(max = 100, message = "커스텀 강의 이름의 최대 글자는 100글자 입니다.")
        String classTitle,

        @Valid
        @Schema(description = "커스텀 강의 시간 정보", requiredMode = REQUIRED)
        List<RequestLectureInfo> lectureInfos,

        @Schema(description = "교수명", example = "교수명", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "교수명의 최대 글자는 30글자 입니다.")
        String professor,

        @Schema(description = "학점", example = "3", requiredMode = NOT_REQUIRED)
        @Size(max = 2, message = "학점은 두 글자 이상일 수 없습니다. (0~9)")
        String grades,

        @Schema(description = "메모", example = "메모", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "메모는 200자 이하로 입력해주세요.")
        String memo
    ) {

    }

    public TimetableLecture toTimetableLecture(TimetableFrame frame) {
        return TimetableLecture.builder()
            .classTitle(timetableLecture.classTitle)
            .professor(timetableLecture.professor)
            .grades(timetableLecture.grades)
            .memo(timetableLecture.memo)
            .classTime(joinClassTimes(timetableLecture.lectureInfos))
            .classPlace(joinClassPlaces(timetableLecture.lectureInfos))
            .timetableFrame(frame)
            .build();
    }
}
