package in.koreatech.koin.admin.student.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminStudentUpdateResponse(
    @Schema(description = "익명 닉네임", example = "익명_1676688416361", requiredMode = NOT_REQUIRED)
    String anonymousNickname,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr", requiredMode = REQUIRED)
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = """
        전공
        - 기계공학부
        - 컴퓨터공학부
        - 메카트로닉스공학부
        - 전기전자통신공학부
        - 디자인공학부
        - 건축공학부
        - 화학생명공학부
        - 에너지신소재공학부
        - 산업경영학부
        - 고용서비스정책학부
        """, example = "컴퓨터공학부", requiredMode = NOT_REQUIRED)
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
    
    public static AdminStudentUpdateResponse from(Student student) {
        User user = student.getUser();

        return new AdminStudentUpdateResponse(
            user.getAnonymousNickname(),
            user.getEmail(),
            user.getGender() != null ? user.getGender().ordinal() : null,
            student.getDepartment() == null ? null : student.getDepartment().getName(),
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            student.getStudentNumber()
        );
    }
}
