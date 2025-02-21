package in.koreatech.koin.domain.student.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentAcademicInfoUpdateResponse(
    @Schema(description = "학번", example = "2021136012", requiredMode = NOT_REQUIRED)
    String studentNumber,

    @Schema(
        description = """
            학부
            - 기계공학부
            - 컴퓨터공학부
            - 메카트로닉스공학부
            - 전기전자통신공학부
            - 디자인공학부
            - 건축공학부
            - 화학생명공학부
            - 에너지신소재공학부
            - 산업경영학부
            - 고용서비스정책학과
            """,
        example = "컴퓨터공학부",
        requiredMode = NOT_REQUIRED
    )
    String department,

    @Schema(
        description = """
                전공:
                - 컴퓨터공학부 (null)
                - 기계공학부 (null)
                - 메카트로닉스공학부 (생산시스템전공, 제어시스템전공, 디지털시스템전공)
                - 전기전자통신공학부 (전기공학전공, 전자공학전공, 정보통신공학전공)
                - 디자인공학부 (디자인공학전공)
                - 건축공학부 (건축공학전공)
                - 화학생명공학부 (화학생명공학전공)
                - 에너지신소재공학부 (에너지신소재공학전공)
                - 산업경영학부 (데이터경영전공, 산업경영전공, 혁신경영전공, 융합경영전공)
                - 고용서비스정책학과 (null)
                - 응용화학공학부 (응용화학공학전공)
            """,
        example = "null",
        requiredMode = NOT_REQUIRED
    )
    String major

) {
    public static StudentAcademicInfoUpdateResponse from(Student student) {
        return new StudentAcademicInfoUpdateResponse(
            student.getStudentNumber(),
            student.getDepartment() == null ? null : student.getDepartment().getName(),
            student.getMajor() == null ? null : student.getMajor().getName()
        );
    }
}
