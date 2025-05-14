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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record RegisterStudentRequestV2(
    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "이름", example = "최준호", requiredMode = REQUIRED)
    @Pattern(regexp = "^(?:[가-힣]{2,5}|[A-Za-z]{2,30})$", message = "한글은 2-5자, 영문은 2-30자 이어야 합니다.")
    String name,

    @Schema(description = "닉네임", example = "캔따개", requiredMode = REQUIRED)
    String nickname,

    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr", requiredMode = REQUIRED)
    @Email(message = "이메일 형식을 지켜주세요. ${validatedValue}")
    String email,

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Schema(description = "휴대폰 번호", example = "01012341234", requiredMode = REQUIRED)
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    String phoneNumber,

    @NotBlank(message = "학번은 필수입니다.")
    @Schema(description = "학번", example = "2025000123", requiredMode = NOT_REQUIRED)
    @Pattern(regexp = "^[0-9]{8,10}$", message = "학번엔 8-10자리 숫자만 입력 가능합니다.")
    String studentNumber,

    @NotBlank(message = "학부는 필수입니다.")
    @Schema(description = "학부", example = "컴퓨터공학부", requiredMode = REQUIRED)
    String department,

    @Schema(description = "성별(남:0, 여:1)", example = "0", requiredMode = NOT_REQUIRED)
    UserGender gender,

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "사용자 아이디", example = "example123", requiredMode = REQUIRED)
    @Pattern(regexp = "^[A-Za-z0-9._-]{5,13}$", message = "5-13자의 영문자, 숫자, 특수문자만 사용할 수 있습니다.")
    String loginId,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6ec7f1...", requiredMode = REQUIRED)
    String password,

    @Schema(description = "마케팅 수신 동의 여부", example = "true", requiredMode = NOT_REQUIRED)
    boolean marketingNotificationAgreement
) {

    public Student toStudent(PasswordEncoder passwordEncoder, Department department) {
        User user = User.builder()
            .name(name)
            .phoneNumber(phoneNumber)
            .userId(loginId)
            .password(passwordEncoder.encode(password))
            .nickname(nickname)
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
