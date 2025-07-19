package in.koreatech.koin.domain.student.dto;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record RegisterStudentRequestV2(
    @Schema(description = "이름", example = "최준호", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^(?:[가-힣]{2,5}|[A-Za-z]{2,30})$", message = "이름은 한글 2-5자 또는 영문 2-30자로 입력해주세요.")
    String name,

    @Schema(description = "닉네임", example = "캔따개", requiredMode = NOT_REQUIRED)
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "한글, 영문 및 숫자만 사용할 수 있습니다.")
    String nickname,

    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr", requiredMode = NOT_REQUIRED)
    @Size(max = 30, message = "이메일의 길이는 최대 30자 입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "휴대폰 번호", example = "01012341234", requiredMode = REQUIRED)
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    String phoneNumber,

    @Schema(description = "성별(남:0, 여:1)", example = "0", requiredMode = REQUIRED)
    @NotNull(message = "성별은 필수입니다.")
    UserGender gender,

    @Schema(
        description = """
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
            - 고용서비스정책학과
            """,
        example = "컴퓨터공학부",
        requiredMode = REQUIRED
    )
    @NotBlank(message = "전공은 필수입니다.")
    String department,

    @Schema(description = "학번", example = "2025000123", requiredMode = REQUIRED)
    @NotBlank(message = "학번은 필수입니다.")
    @Pattern(regexp = "^[0-9]{8,10}$", message = "학번엔 8-10자리 숫자만 입력 가능합니다.")
    String studentNumber,

    @Schema(description = "사용자 아이디", example = "example123", requiredMode = REQUIRED)
    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-z])[a-z0-9._-]{5,13}$", message = "5-13자의 영소문자(필수), 숫자, 특수문자만 사용할 수 있습니다.")
    String loginId,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6...", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 64, max = 64, message = "비밀번호 해시값은 16진수 64자여야 합니다.")
    String password,

    @Schema(description = "마케팅 수신 동의 여부", example = "true", requiredMode = NOT_REQUIRED)
    boolean marketingNotificationAgreement
) {

    public Student toStudent(PasswordEncoder passwordEncoder, Department department) {
        User user = User.builder()
            .name(name)
            .nickname(nickname)
            .phoneNumber(phoneNumber)
            .loginId(loginId)
            .loginPw(passwordEncoder.encode(password))
            .email(email)
            .gender(gender)
            .userType(STUDENT)
            .isAuthed(true)
            .isDeleted(false)
            .deviceToken("TEMPORARY_TOKEN")
            .build();
        Student student = Student.builder()
            .user(user)
            .studentNumber(studentNumber)
            .department(department)
            .build();

        return student;
    }
}
