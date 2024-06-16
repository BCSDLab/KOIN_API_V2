package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureCreateRequest(
    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer timetableFrameId,

    @Valid
    @Schema(description = "강의 시간표 정보", requiredMode = REQUIRED)
    @NotNull(message = "시간표 정보를 입력해주세요.")
    List<InnerTimeTableLectureRequest> timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimeTableLectureRequest(
        @Schema(description = "강의 이름", example = "기상분석", requiredMode = REQUIRED)
        String className,

        @Schema(description = "강의 시간", example = "[210, 211]", requiredMode = REQUIRED)
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "도서관", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(description = "교수명", example = "이강환", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "메모", example = "메모메모", requiredMode = NOT_REQUIRED)
        String memo,

        @Schema(description = "강의 고유 번호", example = "1", requiredMode = NOT_REQUIRED)
        Integer lectureId
    ){

        public TimetableLecture toTimetableLecture(TimetableFrame timetableFrame, Lecture lecture) {
            return new TimetableLecture(
                className,
                Arrays.toString(classTime().stream().toArray()),
                classPlace,
                professor,
                memo,
                false,
                lecture,
                timetableFrame
            );
        }
    }
}
