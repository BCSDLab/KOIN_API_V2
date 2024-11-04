package in.koreatech.koin.domain.timetable.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@JsonNaming(value = SnakeCaseStrategy.class)
public record TimetableUpdateRequest(
    @Valid
    @Schema(description = "시간표 정보", requiredMode = NOT_REQUIRED)
    @NotNull(message = "시간표 정보를 입력해주세요.")
    List<InnerTimetableRequest> timetable,

    @Schema(description = "학기 정보", example = "20192", requiredMode = NOT_REQUIRED)
    @NotBlank(message = "학기 정보를 입력해주세요.")
    String semester
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerTimetableRequest(
        @Schema(description = "시간표 식별 번호", example = "1", requiredMode = REQUIRED)
        @NotNull(message = "시간표 식별 번호를 입력해주세요.")
        Integer id,

        @Schema(description = "과목 코드", example = "CPC490", requiredMode = NOT_REQUIRED)
        String code,

        @Schema(description = "강의 이름", example = "운영체제", requiredMode = REQUIRED)
        @NotBlank(message = "강의 이름을 입력해주세요.")
        String classTitle,

        @Schema(description = "강의 시간", example = "[210, 211]", requiredMode = REQUIRED)
        @NotNull(message = "강의 시간을 입력해주세요.")
        List<Integer> classTime,

        @Schema(description = "강의 장소", example = "null", requiredMode = NOT_REQUIRED)
        String classPlace,

        @Schema(description = "강의 교수", example = "이돈우", requiredMode = NOT_REQUIRED)
        String professor,

        @Schema(description = "학점", example = "3", requiredMode = NOT_REQUIRED)
        String grades,

        @Schema(description = "분반", example = "01", requiredMode = NOT_REQUIRED)
        @Size(max = 3, message = "분반은 3자 이하로 입력해주세요.")
        String lectureClass,

        @Schema(description = "대상", example = "디자 1 건축", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "대상은 200자 이하로 입력해주세요.")
        String target,

        @Schema(description = "수강 인원", example = "25", requiredMode = NOT_REQUIRED)
        @Size(max = 4, message = "수강 인원은 4자 이하로 입력해주세요.")
        String regularNumber,

        @Schema(description = "설계 학점", example = "0", requiredMode = NOT_REQUIRED)
        @Size(max = 4, message = "설계 학점은 4자 이하로 입력해주세요.")
        String designScore,

        @Schema(description = "학부", example = "디자인ㆍ건축공학부", requiredMode = NOT_REQUIRED)
        @Size(max = 30, message = "학부는 30자 이하로 입력해주세요.")
        String department,

        @Schema(description = "memo", example = "메모메모", requiredMode = NOT_REQUIRED)
        @Size(max = 200, message = "메모는 200자 이하로 입력해주세요.")
        String memo
    ) {
        @Builder
        public InnerTimetableRequest {
            if (Objects.isNull(grades)) {
                grades = "0";
            }
        }
    }
}
