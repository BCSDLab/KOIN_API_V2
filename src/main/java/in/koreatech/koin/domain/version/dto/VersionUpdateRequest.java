package in.koreatech.koin.domain.version.dto;

import jakarta.validation.constraints.NotNull;

public record VersionUpdateRequest(
    @NotNull
    // TODO @ApiModelProperty(notes = "버전", example = "1.1.0")
    String version

) {
}
