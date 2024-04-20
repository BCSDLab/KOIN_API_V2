package in.koreatech.koin.domain.version.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.version.model.Version;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record VersionResponse(
    @Schema(description = "버전 ID", example = "1", requiredMode = REQUIRED)
    Integer id,

    @Schema(description = "버전", example = "1.0.0", requiredMode = REQUIRED)
    String version,

    @Schema(description = "버전 타입", example = "android", requiredMode = REQUIRED)
    String type,

    @Schema(description = "생성일", example = "2021-06-21 13:00:00", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,

    @Schema(description = "수정일", example = "2021-06-21", requiredMode = REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDateTime updatedAt
) {

    public static VersionResponse from(Version version) {
        return new VersionResponse(
            version.getId(),
            version.getVersion(),
            version.getType(),
            version.getCreatedAt(),
            version.getUpdatedAt()
        );
    }
}
