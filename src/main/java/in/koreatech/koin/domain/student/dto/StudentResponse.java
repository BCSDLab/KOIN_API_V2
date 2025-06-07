package in.koreatech.koin.domain.student.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentResponse(
    @Schema(example = "1", description = "학생 고유 id")
    Integer id,

    @Schema(example = "example12", description = "학생 로그인 id")
    String loginId,

    @Schema(description = "익명 닉네임", example = "익명_1676688416361")
    String anonymousNickname,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr")
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1")
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
        """, example = "컴퓨터공학부")
    String major,

    @Schema(description = "이름", example = "최준호")
    String name,

    @Schema(description = "닉네임", example = "juno")
    String nickname,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    String phoneNumber,

    @Schema(description = "학번", example = "2029136012")
    String studentNumber,

    @Schema(description = "사용자 타입", example = "STUDENT")
    UserType userType
) {

    public static StudentResponse from(Student student) {
        User user = student.getUser();
        return new StudentResponse(
            student.getId(),
            user.getLoginId(),
            student.getAnonymousNickname(),
            user.getEmail(),
            user.getGender() != null ? user.getGender().ordinal() : null,
            student.getDepartment() == null ? null : student.getDepartment().getName(),
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            student.getStudentNumber(),
            user.getUserType()
        );
    }
}
