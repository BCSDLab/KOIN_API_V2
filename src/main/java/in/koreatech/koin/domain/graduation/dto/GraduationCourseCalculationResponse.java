package in.koreatech.koin.domain.graduation.dto;

import java.util.List;

public record GraduationCourseCalculationResponse(
    List<InnerCalculationResponse> courseTypes
) {

    public static record InnerCalculationResponse(String courseType, int requiredGrades, int grades) {

        public static InnerCalculationResponse of(String courseType, int requiredGrades, int grades) {
            return new InnerCalculationResponse(courseType, requiredGrades, grades);
        }
    }

    public static GraduationCourseCalculationResponse of (List<InnerCalculationResponse> courseTypes) {
        return new GraduationCourseCalculationResponse(courseTypes);
    }
}
