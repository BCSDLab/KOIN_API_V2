package in.koreatech.koin.admin.user.dto;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import org.springframework.security.crypto.password.PasswordEncoder;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
    @Schema(description = "이메일", example = "koin123@koreatech.ac.kr", requiredMode = REQUIRED)
    @Email(message = "이메일 형식을 지켜주세요. ${validatedValue}")
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "SHA 256 해시 알고리즘으로 암호화된 비밀번호", example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(description = "이름", example = "최준호", requiredMode = NOT_REQUIRED)
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z]+$", message = "이름은 한글, 영문만 사용할 수 있습니다.")
    String name,

    @Schema(description = "트랙 이름", example = "백엔드", requiredMode = NOT_REQUIRED)
    @Size(max = 20, message = "트랙 이름은 20자 이내여야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z]+$", message = "트랙 이름은 한글, 영문만 사용할 수 있습니다.")
    String trackName,

    @Schema(description = "팀 이름", example = "비즈니스", requiredMode = NOT_REQUIRED)
    @Size(max = 20, message = "팀 이름은 20자 이내여야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z]+$", message = "팀 이름은 한글, 영문만 사용할 수 있습니다.")
    String teamName
) {
    public Admin toAdmin(PasswordEncoder passwordEncoder) {
        User user = User.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .name(name)
            .userType(ADMIN)
            .isAuthed(false)
            .isDeleted(false)
            .build();

        return Admin.builder()
            .user(user)
            .trackName(trackName)
            .teamName(teamName)
            .build();
    }
}
