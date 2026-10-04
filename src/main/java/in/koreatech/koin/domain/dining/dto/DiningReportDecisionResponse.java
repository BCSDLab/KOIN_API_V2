package in.koreatech.koin.domain.dining.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
@JsonNaming(SnakeCaseStrategy.class)
public record DiningReportDecisionResponse(
    @Schema(description = "대상 제보의 처리 결과. 재호출하면 최초 처리 정보를 유지합니다.", requiredMode = REQUIRED)
    DiningReportResponse report,
    @Schema(description = "처리 묶음 UUID. 함께 처리한 제보가 공유하며 재호출에서도 유지합니다.",
        example = "550e8400-e29b-41d4-a716-446655440000", type = "string", format = "uuid", requiredMode = REQUIRED)
    UUID processingId,
    @Schema(description = "처리 묶음에 속한 제보 ID 목록. 제보 ID 오름차순이며 재호출에서도 최초 목록을 반환합니다.",
        example = "[1, 2]", requiredMode = REQUIRED)
    List<Integer> affectedReportIds,
    @Schema(description = "기존 처리 결과를 반환했는지 여부. 새 처리이면 false, 같은 결과의 재호출이면 true입니다.",
        example = "false", requiredMode = REQUIRED)
    boolean alreadyProcessed
) {
}
