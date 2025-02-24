package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record EducationLectureResponse(
    @Schema(description = "교양 선택")
    SelectiveEducationArea selectiveEducationArea,
    @Schema(description = "글로벌/인성과소양")
    ShaEducationArea shaEducationArea,
    @Schema(description = "일반 교양영역 이수 정보")
    List<RequiredEducationArea> requiredEducationArea
) {
    public record SelectiveEducationArea(
        @Schema(description = "필요학점", example = "3")
        Integer requiredCredit,
        @Schema(description = "이수학점", example = "0")
        Integer completedCredit
    ) {
        public static SelectiveEducationArea of(Integer requiredCredit, Integer completedCredit) {
            return new SelectiveEducationArea(requiredCredit, completedCredit);
        }
    }

    public record ShaEducationArea(
        @Schema(description = "필요학점", example = "2")
        Integer requiredCredit,
        @Schema(description = "이수학점", example = "1")
        Integer completedCredit
    ) {
        public static ShaEducationArea of(Integer requiredCredit, Integer completedCredit) {
            return new ShaEducationArea(requiredCredit, completedCredit);
        }
    }

    public record RequiredEducationArea(
        @Schema(description = "이수구분", example = "사회와심리")
        String courseType,
        @Schema(description = "수강여부", example = "true")
        boolean isCompleted,
        @Schema(description = "강의명", example = "심리학의이해")
        String courseName
    ) {
        public static RequiredEducationArea of(String courseType, boolean isCompleted, String courseName) {
            return new RequiredEducationArea(courseType, isCompleted, courseName);
        }
    }

    public static EducationLectureResponse of(
        SelectiveEducationArea selectiveEducationArea,
        ShaEducationArea shaEducationArea,
        List<RequiredEducationArea> requiredEducationArea
    ) {
        return new EducationLectureResponse(selectiveEducationArea, shaEducationArea, requiredEducationArea);
    }
}
