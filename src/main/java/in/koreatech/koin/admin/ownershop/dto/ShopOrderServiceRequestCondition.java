package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
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

    @Schema(description = "가게 이름 검색 문자열", example = "티바", requiredMode = NOT_REQUIRED)
    String query,

    @Schema(description = "상점 서비스 요청 상태[`PENDING` (대기), `APPROVED` (승인), `REJECTED` (거절)]", example = "PENDING", requiredMode = NOT_REQUIRED)
    ShopOrderServiceRequestStatus status,

    @Schema(description = "정렬 기준[`CREATED_AT_ASC` (오래된순), `CREATED_AT_DESC` (최신순)]", example = "CREATED_AT_ASC", defaultValue = "CREATED_AT_ASC", requiredMode = NOT_REQUIRED)
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

    public void checkDataConstraintViolation() {
        if (this.query != null) {
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

    private void checkQueryIsBlank() {
        if (StringUtils.isBlank(this.query)) {
            throw CustomException.of(SEARCH_QUERY_ONLY_WHITESPACE);
        }
    }

    public boolean isQueryNotNull() {
        return this.query != null;
    }

    public boolean isStatusNotNull() {
        return this.status != null;
    }
}
