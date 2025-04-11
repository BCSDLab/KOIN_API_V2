package in.koreatech.koin.admin.user.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminResponse(
    @Schema(description = "고유 id", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이메일", example = "koin00001@koreatech.ac.kr", requiredMode = REQUIRED)
    String email,

    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    String name,

    @Schema(description = "트랙 이름", example = "Backend", requiredMode = REQUIRED)
    String trackName,

    @Schema(description = "팀 이름", example = "User", requiredMode = REQUIRED)
    String teamName
) {
    public static AdminResponse from(Admin admin) {
        User user = admin.getUser();

        return new AdminResponse(
            admin.getId(),
            user.getEmail(),
            user.getName(),
            admin.getTrackType().getValue(),
            admin.getTeamType().getValue()
        );
    }
}
