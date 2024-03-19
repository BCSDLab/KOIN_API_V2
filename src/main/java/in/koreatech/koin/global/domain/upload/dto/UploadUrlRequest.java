package in.koreatech.koin.global.domain.upload.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadUrlRequest(
    Integer contentLength,
    String contentType,
    String fileName
) {

}
