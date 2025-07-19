package in.koreatech.koin.domain.student.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentWithAcademicResponse(
    @Schema(example = "1", description = "학생 고유 id", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "익명 닉네임", example = "익명_1676688416361", requiredMode = NOT_REQUIRED)
    String anonymousNickname,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr", requiredMode = NOT_REQUIRED)
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

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
    String major,

    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "닉네임", example = "juno", requiredMode = NOT_REQUIRED)
    String nickname,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000", requiredMode = NOT_REQUIRED)
    String phoneNumber,

    @Schema(description = "학번", example = "2029136012", requiredMode = NOT_REQUIRED)
    String studentNumber
) {

    public static StudentWithAcademicResponse from(Student student) {
        User user = student.getUser();
        Integer userGender = null;
        if (user.getGender() != null) {
            userGender = user.getGender().ordinal();
        }
        return new StudentWithAcademicResponse(
            student.getId(),
            user.getAnonymousNickname(),
            user.getEmail(),
            userGender,
            student.getDepartment() == null ? null : student.getDepartment().getName(),
            student.getMajor() == null ? null : student.getMajor().getName(),
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            student.getStudentNumber()
        );
    }
}
