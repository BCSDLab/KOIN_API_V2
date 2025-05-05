package in.koreatech.koin.infrastructure.s3.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadUrlRequest(
    @Schema(description = "파일 크기", example = "1000")
    Integer contentLength,

    @Schema(description = "파일 타입", example = "image/png")
    String contentType,

    @Schema(description = "파일 이름", example = "hello.png")
    String fileName
) {

}
