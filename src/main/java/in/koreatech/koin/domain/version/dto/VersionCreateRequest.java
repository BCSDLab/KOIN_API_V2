package in.koreatech.koin.domain.version.dto;

import jakarta.validation.constraints.NotNull;

public record VersionCreateRequest(
    @NotNull
    // TODO @ApiModelProperty(notes = "버전", example = "1.1.0")
    String version,

    @NotNull
    // TODO @ApiModelProperty(notes = "타입", example = "android")
    String type
) {
}
