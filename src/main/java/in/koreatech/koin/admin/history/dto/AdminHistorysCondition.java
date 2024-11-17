package in.koreatech.koin.admin.history.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminHistorysCondition(
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "HTTP 메소드", example = "POST", requiredMode = NOT_REQUIRED)
    String requestMethod,

    @Schema(description = "도메인 이름", example = "NOTICE", requiredMode = NOT_REQUIRED)
    String domainName,

    @Schema(description = "특정 엔티티 id", requiredMode = NOT_REQUIRED)
    Integer domainId
) {
    public AdminHistorysCondition {
        if (Objects.isNull(page)) {
            page = Criteria.DEFAULT_PAGE;
        }
        if (Objects.isNull(limit)) {
            limit = Criteria.DEFAULT_LIMIT;
        }
    }
}
