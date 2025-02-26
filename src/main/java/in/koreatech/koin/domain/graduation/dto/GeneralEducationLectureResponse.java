package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record GeneralEducationLectureResponse(
    @Schema(description = "교양영역 이수 정보")
    List<GeneralEducationArea> generalEducationArea
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record GeneralEducationArea(
        @Schema(description = "이수구분", example = "인성과소양")
        String courseType,

        @Schema(description = "필요학점", example = "2")
        Integer requiredCredit,

        @Schema(description = "이수학점", example = "2")
        Integer completedCredit,

        @Schema(description = "이수강의", example = "[대학생활과비전, 견행학]")
        List<String> courseNames
    ) {
        public static GeneralEducationArea of(
            String courseType,
            Integer requiredCredit,
            Integer completedCredit,
            List<String> courseNames
        ) {
            return new GeneralEducationArea(courseType, requiredCredit, completedCredit, courseNames);
        }
    }

    public static GeneralEducationLectureResponse of(
        List<GeneralEducationArea> generalEducationArea
    ) {
        return new GeneralEducationLectureResponse(generalEducationArea);
    }
}
