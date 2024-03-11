package in.koreatech.koin.domain.version.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.version.model.Version;

@JsonNaming(SnakeCaseStrategy.class)
public record VersionResponse(
    Long id,
    String version,
    String type,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

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
