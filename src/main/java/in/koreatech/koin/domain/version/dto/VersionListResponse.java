package in.koreatech.koin.domain.version.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import in.koreatech.koin.domain.version.model.Version;

public class VersionListResponse {
    private List<InnerVersionResponse> versions;

    public static VersionListResponse from(List<Version> versions) {
        VersionListResponse response = new VersionListResponse();
        response.versions = versions.stream()
                                    .map(InnerVersionResponse::new)
                                    .collect(Collectors.toList());
        return response;
    }

    public static class InnerVersionResponse {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("version")
        private String version;

        @JsonProperty("type")
        private String type;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        public InnerVersionResponse(Version versionEntity) {
            this.id = versionEntity.getId();
            this.version = versionEntity.getVersion();
            this.type = versionEntity.getType();
            this.createdAt = versionEntity.getCreatedAt();
            this.updatedAt = versionEntity.getUpdatedAt();
        }
    }
}
