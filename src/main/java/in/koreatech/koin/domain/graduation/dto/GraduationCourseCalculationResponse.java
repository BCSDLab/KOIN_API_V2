package in.koreatech.koin.domain.graduation.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = SnakeCaseStrategy.class)
public record GraduationCourseCalculationResponse(

    List<InnerCalculationResponse> courseTypes
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerCalculationResponse(String courseType, int requiredGrades, int grades) {

        public static InnerCalculationResponse of(String courseType, int requiredGrades, int grades) {
            return new InnerCalculationResponse(courseType, requiredGrades, grades);
        }
    }

    public static GraduationCourseCalculationResponse of (List<InnerCalculationResponse> courseTypes) {
        return new GraduationCourseCalculationResponse(courseTypes);
    }
}
