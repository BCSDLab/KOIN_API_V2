package in.koreatech.koin.domain.version.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import in.koreatech.koin.domain.version.model.Version;

public record VersionResponse(
    @JsonProperty("id") Long id,
    @JsonProperty("version") String version,
    @JsonProperty("type") String type,
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
