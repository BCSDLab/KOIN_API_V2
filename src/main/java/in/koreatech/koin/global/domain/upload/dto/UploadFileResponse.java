package in.koreatech.koin.global.domain.upload.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UploadFileResponse(
    @Schema(description = "", example = "")
    String fileUrl
) {

}
