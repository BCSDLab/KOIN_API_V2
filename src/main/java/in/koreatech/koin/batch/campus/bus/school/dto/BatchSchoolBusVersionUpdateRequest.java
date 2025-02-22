package in.koreatech.koin.batch.campus.bus.school.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BatchSchoolBusVersionUpdateRequest(
    @Schema(description = "업데이트 제목", example = "방학기간")
    String title,

    @Schema(description = "업데이트 내용", example = "2025-01-15~2025-02-28")
    String content
) {
}
