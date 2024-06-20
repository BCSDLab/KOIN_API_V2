package in.koreatech.koin.domain.timetableV2.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableLectureUpdateRequest(
    @Schema(description = "시간표 프레임 id", example = "1", requiredMode = REQUIRED)
    @NotNull(message = "시간표 식별 번호를 입력해주세요.")
    Integer timetableFrameId,

    @Valid
    @Schema(description = "시간표 정보", requiredMode = NOT_REQUIRED)
    @NotNull(message = "시간표 정보를 입력해주세요.")
    List<InnerTimetableLectureRequest> timetableLecture
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableLectureRequest(
        @Schema(description = "시간표 강의 id", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "강의 id", example = "1", requiredMode = NOT_REQUIRED)
        Integer lectureId,

        @Schema(description = "강의 이름", example = "운영체제", requiredMode = NOT_REQUIRED)
        String classTitle,

        @Schema(description = "강의 시간", example = "[210, 211]", requiredMode = NOT_REQUIRED)
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "null", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(name = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "학점", example = "3", requiredMode = NOT_REQUIRED)
        String grades,

        @Schema(name = "memo", example = "메모메모", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "메모는 200자 이하로 입력해주세요.")
        String memo
    ) {

    }
}
