package in.koreatech.koin.infrastructure.s3.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadUrlRequest(
    @Schema(description = "파일 크기", example = "1000", requiredMode = REQUIRED)
    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 0보다 커야 합니다.")
    Integer contentLength,

    @Schema(description = "파일 타입", example = "image/png", requiredMode = REQUIRED)
    @NotBlank(message = "파일 타입은 필수입니다.")
    String contentType,

    @Schema(description = "파일 이름", example = "hello.png", requiredMode = REQUIRED)
    @NotBlank(message = "파일 이름은 필수입니다.")
    String fileName
) {

}
