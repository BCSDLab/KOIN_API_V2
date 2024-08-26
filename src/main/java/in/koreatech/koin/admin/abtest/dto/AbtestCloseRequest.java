package in.koreatech.koin.admin.abtest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestCloseRequest(
    @Schema(description = "승자 이름", example = "A")
    String winnerName
) {
}
