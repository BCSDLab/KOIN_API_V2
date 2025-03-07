package in.koreatech.koin.integration.s3.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record UploadFileResponse(
    @Schema(description = "첨부 파일 URL", example = "https://static.koreatech.in/1.png")
    String fileUrl
) {

}
