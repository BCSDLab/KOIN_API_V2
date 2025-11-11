package in.koreatech.koin.admin.history.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static in.koreatech.koin.common.model.Criteria.*;
import static in.koreatech.koin.common.model.Criteria.Sort.CREATED_AT_DESC;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.history.enums.HttpMethodType;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminHistoriesCondition(
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "HTTP 메소드", example = "POST", requiredMode = NOT_REQUIRED)
    HttpMethodType requestMethod,

    @Schema(description = "도메인 이름", example = "NOTICE", requiredMode = NOT_REQUIRED)
    DomainType domainName,

    @Schema(description = "특정 엔티티 id", requiredMode = NOT_REQUIRED)
    Integer domainId,

    @Schema(description = "정렬 기준", requiredMode = NOT_REQUIRED)
    Sort sort
) {
    public AdminHistoriesCondition {
        if (Objects.isNull(page)) {
            page = DEFAULT_PAGE;
        }
        if (Objects.isNull(limit)) {
            limit = DEFAULT_LIMIT;
        }
        if (Objects.isNull(sort)) {
            sort = CREATED_AT_DESC;
        }
    }
}
