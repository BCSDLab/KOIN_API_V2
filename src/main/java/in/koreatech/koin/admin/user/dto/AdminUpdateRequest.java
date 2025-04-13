package in.koreatech.koin.admin.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.user.enums.TeamType;
import in.koreatech.koin.admin.user.enums.TrackType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminUpdateRequest(
    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    String name,

    @Schema(description = "이메일", example = "koin00001@koreatech.ac.kr", requiredMode = REQUIRED)
    @NotBlank(message = "이메일을 입력해주세요.")
    String email,

    @Schema(description = "전화번호", example = "010-4255-1540", requiredMode = REQUIRED)
    @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    String phoneNumber,

    @Schema(description = "트랙 타입", example = "BACKEND", requiredMode = REQUIRED)
    @NotNull(message = "트랙 타입은 필수 입력 사항입니다.")
    TrackType trackType,

    @Schema(description = "팀 타입", example = "USER", requiredMode = REQUIRED)
    @NotNull(message = "팀 타입은 필수 입력 사항입니다.")
    TeamType teamType
) {
}
