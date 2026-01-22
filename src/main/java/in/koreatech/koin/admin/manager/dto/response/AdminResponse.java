package in.koreatech.koin.admin.manager.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.manager.model.Admin;
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

    @Schema(description = "슈퍼 어드민 권한", example = "false", requiredMode = REQUIRED)
    Boolean superAdmin,

    @Schema(description = "활성화 여부", example = "true", requiredMode = REQUIRED)
    Boolean isAuthed
) {
    public static AdminResponse from(Admin admin) {
        User user = admin.getUser();

        return new AdminResponse(
            admin.getId(),
            admin.getEmail(),
            user.getName(),
            admin.getTrackType().getValue(),
            admin.isSuperAdmin(),
            user.isAuthed()
        );
    }
}
