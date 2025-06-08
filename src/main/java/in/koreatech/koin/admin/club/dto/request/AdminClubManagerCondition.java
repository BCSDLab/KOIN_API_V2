package in.koreatech.koin.admin.club.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin._common.model.Criteria;
import in.koreatech.koin._common.model.Criteria.Sort;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminClubManagerCondition(
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "정렬 기준 [`CREATED_AT_ASC`, `CREATED_AT_DESC`]", example = "CREATED_AT_DESC", defaultValue = "CREATED_AT_DESC", requiredMode = NOT_REQUIRED)
    Sort sort
) {
    public AdminClubManagerCondition {
        if (Objects.isNull(page))
            page = Criteria.DEFAULT_PAGE;
        if (Objects.isNull(limit))
            limit = Criteria.DEFAULT_LIMIT;
        if (Objects.isNull(sort))
            sort = Sort.CREATED_AT_DESC;
    }

    @Hidden
    public Direction getDirection() {
        return this.sort == Sort.CREATED_AT_ASC ? Direction.ASC : Direction.DESC;
    }
}
