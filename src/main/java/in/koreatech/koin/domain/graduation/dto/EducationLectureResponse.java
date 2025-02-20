package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record EducationLectureResponse(
    @Schema(description = "교양영역 이수 정보")
    List<RequiredEducationArea> requiredEducationArea
) {
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

    public static EducationLectureResponse of(List<RequiredEducationArea> requiredEducationArea) {
        return new EducationLectureResponse(requiredEducationArea);
    }
}
