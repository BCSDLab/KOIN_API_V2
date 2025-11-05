package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.global.exception.CustomException;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;

import static in.koreatech.koin.global.code.ApiResponseCode.*;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ShopOrderServiceRequestCondition(
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "검색 대상[`SHOP_NAME` (상점명 검색), `STATUS` (상태 검색)]", example = "SHOP_NAME", requiredMode = NOT_REQUIRED)
    SearchType searchType,

    @Schema(description = "검색 문자열 [상태일경우 `PENDING`, `APPROVED`, `REJECTED` 중 하나]", example = "티바", requiredMode = NOT_REQUIRED)
    String query,

    @Schema(description = "정렬 기준['CREATED_AT_ASC` (오래된순), 'CREATED_AT_DESC` (최신순)]", example = "CREATED_AT_ASC", defaultValue = "CREATED_AT_ASC", requiredMode = NOT_REQUIRED)
    Criteria.Sort sort
) {

    public ShopOrderServiceRequestCondition {
        if (Objects.isNull(page)) {
            page = Criteria.DEFAULT_PAGE;
        }
        if (Objects.isNull(limit)) {
            limit = Criteria.DEFAULT_LIMIT;
        }
        if (Objects.isNull(sort)) {
            sort = Criteria.Sort.CREATED_AT_ASC;
        }
    }

    public enum SearchType { //이거 예외 처리도.?
        SHOP_NAME,
        STATUS;
    }

    public void checkDataConstraintViolation() {
        if (this.query != null) {
            checkSearchTypeNotNull();
            checkQueryIsBlank();
        }
    }

    @Hidden
    public Direction getDirection() {
        if (this.sort == Criteria.Sort.CREATED_AT_ASC) {
            return Direction.ASC;
        } else {
            return Direction.DESC;
        }
    }

    private void checkSearchTypeNotNull() {
        if (this.searchType == null) {
            throw CustomException.of(REQUIRED_SEARCH_TYPE);
        }
    }

    private void checkQueryIsBlank() {
        if (StringUtils.isBlank(this.query)) {
            throw CustomException.of(SEARCH_QUERY_ONLY_WHITESPACE);
        }
    }
}
