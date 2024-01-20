package in.koreatech.koin.domain.version.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.version.model.Version;
import in.koreatech.koin.domain.version.model.VersionType;

@JsonNaming(value = SnakeCaseStrategy.class)
public record VersionResponse(
    @JsonProperty("id") Long id,
    @JsonProperty("version") String version,
    @JsonProperty("type") VersionType type,
    @JsonProperty("created_at") LocalDateTime createdAt,
    @JsonProperty("updated_at") LocalDateTime updatedAt) {

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
