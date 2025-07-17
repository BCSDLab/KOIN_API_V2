package in.koreatech.koin.admin.student.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminStudentResponse (

    @Schema(description = "id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "닉네임", example = "seongjae", requiredMode = NOT_REQUIRED)
    String nickname,

    @Schema(description = "이름", example = "김성재", requiredMode = NOT_REQUIRED)
    String name,

    @Schema(description = "휴대폰 번호", example = "010-0000-0000", requiredMode = NOT_REQUIRED)
    String phoneNumber,

    @Schema(description = "유저 타입", example = "STUDENT", requiredMode = REQUIRED)
    String userType,

    @Schema(description = "이메일 주소", example = "koin123@koreatech.ac.kr", requiredMode = REQUIRED)
    String email,

    @Schema(description = "성별(남:0, 여:1)", example = "1", requiredMode = NOT_REQUIRED)
    Integer gender,

    @Schema(description = "인증 여부", example = "true", requiredMode = REQUIRED)
    Boolean isAuthed,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "마지막 로그인 시간", example = "2024-01-15 12:00:00", requiredMode = NOT_REQUIRED)
    LocalDateTime lastLoggedAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "생성 일자", example = "2024-01-15 12:00:00", requiredMode = REQUIRED)
    LocalDateTime createdAt,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "수정 일자", example = "2024-01-15 12:00:00", requiredMode = REQUIRED)
    LocalDateTime updatedAt,

    @Schema(description = "익명 닉네임", example = "익명_1676688416361", requiredMode = NOT_REQUIRED)
    String anonymousNickname,

    @Schema(description = "학번", example = "2020174015", requiredMode = NOT_REQUIRED)
    String studentNumber,

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

    @Schema(description = "졸업 여부", example = "false", requiredMode = NOT_REQUIRED)
    Boolean isGraduated
) {
    public static AdminStudentResponse from(Student student) {
        User user = student.getUser();

        return new AdminStudentResponse(
            user.getId(),
            user.getNickname(),
            user.getName(),
            user.getPhoneNumber(),
            user.getUserType().toString(),
            user.getEmail(),
            user.getGender() == null ? null : user.getGender().ordinal(),
            user.isAuthed(),
            user.getLastLoggedAt(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getAnonymousNickname(),
            student.getStudentNumber(),
            student.getDepartment() == null ? null : student.getDepartment().getName(),
            student.isGraduated()
        );
    }
}
