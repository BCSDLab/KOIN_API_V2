package in.koreatech.koin.admin.manager.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.manager.enums.TeamType;
import in.koreatech.koin.admin.manager.enums.TrackType;
import in.koreatech.koin.admin.manager.model.Admin;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CreateAdminRequest(
    @Schema(description = "이메일", example = "koin00001@koreatech.ac.kr", requiredMode = REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "비밀번호 (SHA 256 해싱된 값)", example = "cd06f8c2b0dd065faf6...", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    @NotBlank(message = "이름을 입력해주세요.")
    String name,

    @Schema(description = "트랙 타입", example = "BACKEND", requiredMode = REQUIRED)
    @NotNull(message = "트랙 타입을 입력해주세요.")
    TrackType trackType,

    @Schema(description = "팀 타입", example = "USER", requiredMode = REQUIRED)
    @NotNull(message = "팀 타입을 입력해주세요.")
    TeamType teamType
) {

    public Admin toAdmin(PasswordEncoder passwordEncoder) {
        String loginId = email.substring(0, email.indexOf("@"));
        User user = User.builder()
            .loginPw(passwordEncoder.encode(password))
            .name(name)
            .userType(ADMIN)
            .isAuthed(false)
            .isDeleted(false)
            .build();

        return Admin.builder()
            .email(email)
            .loginId(loginId)
            .trackType(trackType)
            .teamType(teamType)
            .user(user)
            .build();
    }
}
