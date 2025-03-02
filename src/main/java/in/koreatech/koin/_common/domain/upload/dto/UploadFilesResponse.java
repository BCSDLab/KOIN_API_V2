package in.koreatech.koin._common.domain.upload.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record UploadFilesResponse(
    List<String> fileUrls
) {

}
