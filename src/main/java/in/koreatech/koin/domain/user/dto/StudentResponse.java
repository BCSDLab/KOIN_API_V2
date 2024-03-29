package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record StudentResponse(
    @Schema(description = "익명 닉네임", example = "익명_1676688416361")
    String anonymousNickname,

    @Schema(description = "이메일 주소 \n", example = "koin123@koreatech.ac.kr")
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1")
    String gender,

    @Schema(description = "전공{기계공학부, 컴퓨터공학부, 메카트로닉스공학부, 전기전자통신공학부, 디자인건축공학부, 에너지신소재화학공학부, 산업경영학부}", example = "컴퓨터공학부")
    String major,

    @Schema(description = "이름 \n", example = "최준호")
    String name,

    @Schema(description = "닉네임", example = "juno")
    String nickname,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    String phoneNumber,

    @Schema(description = "학번", example = "2029136012")
    String studentNumber
) {

    public static StudentResponse from(Student student) {
        User user = student.getUser();
        return new StudentResponse(
            student.getAnonymousNickname(),
            user.getEmail(),
            user.getGender().name(),
            student.getDepartment(),
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            student.getStudentNumber()
        );
    }
}
