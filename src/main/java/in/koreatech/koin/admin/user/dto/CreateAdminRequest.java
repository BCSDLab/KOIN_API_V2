package in.koreatech.koin.admin.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.user.enums.Role;
import in.koreatech.koin.admin.user.enums.TeamType;
import in.koreatech.koin.admin.user.enums.TrackType;
import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonNaming(value = SnakeCaseStrategy.class)
public record CreateAdminRequest(
    @Schema(description = "이메일", example = "koin00001@koreatech.ac.kr", requiredMode = REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "SHA 256 해시 알고리즘으로 암호화된 비밀번호", example = "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268", requiredMode = REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    String password,

    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    @Size(max = 50, message = "이름은 50자 이내여야 합니다.")
    @NotBlank(message = "이름을 입력해주세요.")
    String name,

    @Schema(description = "전화번호", example = "010-4255-1540", requiredMode = REQUIRED)
    @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    String phoneNumber,

    @Schema(description = "트랙 타입", example = "BACKEND", requiredMode = REQUIRED)
    @NotNull(message = "트랙 타입을 입력해주세요.")
    TrackType trackType,

    @Schema(description = "팀 타입", example = "USER", requiredMode = REQUIRED)
    @NotNull(message = "팀 타입을 입력해주세요.")
    TeamType teamType,

    @Schema(description = "직함", example = "TRACK_REGULAR", requiredMode = REQUIRED)
    @NotNull(message = "직함을 입력해주세요.")
    Role role
) {

    public Admin toAdmin(User user) {
        return Admin.builder()
            .trackType(trackType)
            .teamType(teamType)
            .role(role)
            .email(email)
            .name(name)
            .phoneNumber(phoneNumber)
            .user(user)
            .build();
    }

    public User toUser(PasswordEncoder passwordEncoder) {
        String userId = email.substring(0, email.indexOf("@"));
        return User.builder()
            .email(email)
            .password(passwordEncoder.encode(password))
            .userId(userId)
            .name(name)
            .userType(ADMIN)
            .isAuthed(true)
            .isDeleted(false)
            .build();
    }
}
