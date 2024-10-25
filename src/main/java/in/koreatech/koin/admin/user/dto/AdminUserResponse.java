package in.koreatech.koin.admin.user.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import in.koreatech.koin.admin.user.model.Admin;
import in.koreatech.koin.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminUserResponse(
    @Schema(description = "번호", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "이메일", example = "soundbar91@gmail.com", requiredMode = REQUIRED)
    String email,

    @Schema(description = "이름", example = "신관규", requiredMode = REQUIRED)
    String name,

    @Schema(description = "트랙 이름", example = "백엔드", requiredMode = REQUIRED)
    String trackName,

    @Schema(description = "팀 이름", example = "유저", requiredMode = REQUIRED)
    String teamName,

    @Schema(description = "인증 여부", example = "true", requiredMode = REQUIRED)
    Boolean isAuthed,

    @Schema(description = "유저 타입", example = "ADMIN", requiredMode = REQUIRED)
    String userType,

    @Schema(description = "created_at", example = "2018-09-02 21:48:14", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "updated_at", example = "2024-03-03 14:24:40", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,

    @Schema(description = "last_logged_at", example = "2018-09-03 06:50:28", requiredMode = NOT_REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime lastLoggedAt
) {
    public static AdminUserResponse of(Admin admin) {
        User user = admin.getUser();

        return new AdminUserResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            admin.getTrackName(),
            admin.getTeamName(),
            user.isAuthed(),
            user.getUserType().getValue(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLoggedAt()
        );
    }
}
