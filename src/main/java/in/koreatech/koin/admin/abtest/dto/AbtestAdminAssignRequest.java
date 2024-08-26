package in.koreatech.koin.admin.abtest.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestAdminAssignRequest(
    @NotNull(message = "디바이스 ID는 필수입니다.")
    @Schema(description = "디바이스 ID", example = "1")
    Integer deviceId,

    @NotNull(message = "테스트 변수명은 필수입니다.")
    @Schema(description = "테스트 변수명", example = "A")
    String variableName
) {
}
