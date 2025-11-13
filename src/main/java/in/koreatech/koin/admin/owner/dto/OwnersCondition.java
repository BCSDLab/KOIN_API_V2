package in.koreatech.koin.admin.owner.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.common.model.Criteria.Sort;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OwnersCondition(
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "검색 대상[`EMAIL` (이메일 검색), NAME` (이름 검색)]", example = "EMAIL", requiredMode = NOT_REQUIRED)
    SearchType searchType,

    @Schema(description = "검색 문자열", requiredMode = NOT_REQUIRED)
    String query,

    @Schema(description = "정렬 기준['CREATED_AT_ASC` (오래된순), 'CREATED_AT_DESC` (최신순)]", example = "CREATED_AT_ASC", defaultValue = "CREATED_AT_ASC", requiredMode = NOT_REQUIRED)
    Sort sort
) {
    public OwnersCondition {
        if (Objects.isNull(page)) {
            page = Criteria.DEFAULT_PAGE;
        }
        if (Objects.isNull(limit)) {
            limit = Criteria.DEFAULT_LIMIT;
        }
        if (Objects.isNull(sort)) {
            sort = Sort.CREATED_AT_ASC;
        }
    }

    public enum SearchType {
        EMAIL,
        NAME
    }

    public void checkDataConstraintViolation() {
        if (this.query != null) {
            checkSearchTypeNotNull();
            checkQueryIsEmpty();
            checkQueryIsBlank();
        }
    }

    private void checkSearchTypeNotNull() {
        if (this.searchType == null) {
            throw new KoinIllegalArgumentException("검색 내용이 존재할 경우 검색 대상은 필수입니다.");
        }
    }

    private void checkQueryIsEmpty() {
        if (this.query.length() == 0) {
            throw new KoinIllegalArgumentException("검색 내용의 최소 길이는 1입니다.");
        }
    }

    private void checkQueryIsBlank() {
        if (StringUtils.isBlank(this.query)) {
            throw new KoinIllegalArgumentException("검색 내용은 공백 문자로만 이루어져 있으면 안됩니다.");
        }
    }
}
