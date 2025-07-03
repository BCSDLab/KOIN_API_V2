package in.koreatech.koin.domain.student.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record RegisterStudentRequest(
    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr", requiredMode = REQUIRED)
    @Email(message = "이메일 형식을 지켜주세요. ${validatedValue}")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글, 영문만 사용할 수 있습니다.")
    String name,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6...", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(description = "닉네임", example = "juno", requiredMode = NOT_REQUIRED)
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "한글, 영문 및 숫자만 사용할 수 있습니다.")
    String nickname,

    @Schema(description = "성별(남:0, 여:1)", example = "0", requiredMode = NOT_REQUIRED)
    UserGender gender,

    @Schema(description = "졸업 여부", example = "false", requiredMode = NOT_REQUIRED)
    boolean isGraduated,

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
    String major,

    @Schema(description = "학번", example = "2021136012", requiredMode = NOT_REQUIRED)
    @Pattern(regexp = "^[0-9]{10}$", message = "학번엔 10자리 숫자만 입력 가능합니다.")
    String studentNumber,

    @Schema(description = "휴대폰 번호", example = "010-1234-5678 또는 01012345678", requiredMode = NOT_REQUIRED)
    @Pattern(regexp = "^(\\d{3}-\\d{3,4}-\\d{4}|\\d{10,11})$", message = "전화번호 형식이 올바르지 않습니다.")
    String phoneNumber
) {

}
