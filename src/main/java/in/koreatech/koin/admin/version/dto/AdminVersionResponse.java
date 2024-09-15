package in.koreatech.koin.admin.version.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminVersionResponse (
    @Schema(description = "버전", example = "3.5.1", requiredMode = Schema.RequiredMode.REQUIRED)
    String version,

    @Schema(description = "문구", example = "업데이트 해주세요!", requiredMode = Schema.RequiredMode.REQUIRED)
    String message
){

}
