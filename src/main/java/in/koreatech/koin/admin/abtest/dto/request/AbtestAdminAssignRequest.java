package in.koreatech.koin.admin.abtest.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record AbtestAdminAssignRequest(
    @NotNull(message = "디바이스 ID는 필수입니다.")
    @Schema(description = "디바이스 ID", example = "1")
    Integer deviceId,

    @NotNull(message = "실험군 변수명은 필수입니다.")
    @Size(min = 1, max = 255, message = "실험군 변수명은 1자 이상 255자 이하로 입력해주세요.")
    @Schema(description = "실험군 변수명", example = "A")
    String variableName
) {
}
