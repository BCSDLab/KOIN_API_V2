package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record EducationLectureResponse(
    List<RequiredEducationArea> requiredEducationArea
) {
    public record RequiredEducationArea(
        String courseType,
        boolean isCompleted,
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