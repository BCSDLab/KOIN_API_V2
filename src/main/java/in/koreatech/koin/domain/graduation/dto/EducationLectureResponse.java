package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record EducationLectureResponse(
    SelectiveEducationArea selectiveEducationArea,
    List<RequiredEducationArea> requiredEducationArea
) {
    public record SelectiveEducationArea(
        int requiredCredit,
        int completedCredit
    ) {
    }

    public record RequiredEducationArea(
        String courseType,
        boolean isCompleted,
        String courseName
    ) {
    }
}
