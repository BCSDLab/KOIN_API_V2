package in.koreatech.koin.domain.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.user.model.UserGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UserUpdateRequest(
    @Schema(description = "이름", example = "최준호", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^(?:[가-힣]{2,5}|[A-Za-z]{2,30})$", message = "한글은 2-5자, 영문은 2-30자 이어야 합니다.")
    String name,

    @Schema(description = "닉네임", example = "캔따개", requiredMode = NOT_REQUIRED)
    @Size(max = 10, message = "닉네임은 최대 10자입니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]+$", message = "한글, 영문 및 숫자만 사용할 수 있습니다.")
    String nickname,

    @Schema(description = "이메일 주소", example = "general123@naver.com", requiredMode = NOT_REQUIRED)
    @Size(max = 30, message = "이메일의 길이는 최대 30자 입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @Schema(description = "휴대폰 번호", example = "010-1234-5678 또는 01012345678", requiredMode = REQUIRED)
    @NotBlank(message = "휴대폰 번호는 필수 입력입니다.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호 형식이 올바르지 않습니다. 11자리 숫자로 입력해 주세요.")
    String phoneNumber,

    @Schema(description = "성별(남:0, 여:1)", example = "0", requiredMode = REQUIRED)
    @NotNull(message = "성별은 필수입니다.")
    UserGender gender,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6...", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 64, max = 64, message = "비밀번호 해시값은 16진수 64자여야 합니다.")
    String password
) {

}
