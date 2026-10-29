package in.koreatech.koin.domain.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserProfileResponse(
    @Schema(example = "1", description = "사용자 고유 id")
    Integer id,

    @Schema(example = "example12", description = "로그인 id")
    String loginId,

    @Schema(description = "익명 닉네임", example = "익명_1676688416361")
    String anonymousNickname,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr")
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1")
    Integer gender,

    @Schema(description = "이름", example = "최준호")
    String name,

    @Schema(description = "닉네임", example = "juno")
    String nickname,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000")
    String phoneNumber,

    @Schema(description = "사용자 타입. 학생·총학생회는 학생 정보가 함께 내려옵니다.", example = "STUDENT")
    UserType userType,

    @Schema(description = "학번. 학생·총학생회만 존재합니다.", example = "2029136012", nullable = true)
    String studentNumber,

    @Schema(description = "전공(학부). 학생·총학생회만 존재합니다.", example = "컴퓨터공학부", nullable = true)
    String major
) {

    public static UserProfileResponse from(User user) {
        return of(user, null, null);
    }

    public static UserProfileResponse from(Student student) {
        String major = student.getDepartment() == null ? null : student.getDepartment().getName();
        return of(student.getUser(), student.getStudentNumber(), major);
    }

    private static UserProfileResponse of(User user, String studentNumber, String major) {
        return new UserProfileResponse(
            user.getId(),
            user.getLoginId(),
            user.getAnonymousNickname(),
            user.getEmail(),
            user.getGender() != null ? user.getGender().ordinal() : null,
            user.getName(),
            user.getNickname(),
            user.getPhoneNumber(),
            user.getUserType(),
            studentNumber,
            major
        );
    }
}
