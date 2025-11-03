package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.global.exception.custom.KoinIllegalArgumentException;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ShopOrderServiceRequestCondition(
    @Schema(description = "페이지", example = "1", defaultValue = "1", requiredMode = NOT_REQUIRED)
    Integer page,

    @Schema(description = "페이지당 조회할 최대 개수", example = "10", defaultValue = "10", requiredMode = NOT_REQUIRED)
    Integer limit,

    @Schema(description = "검색 대상") // 추가적으로 작성하기
    ShopOrderServiceRequestCondition.SearchType searchType,

    @Schema(description = "검색 문자열", requiredMode = NOT_REQUIRED)
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

    public enum SearchType {
        //음... 작성하기
    }

    // 일단은 OwnersCondition과 동일하게 작성..
    private void checkSearchTypeNotNull() {
        if (this.searchType == null) {
            throw new KoinIllegalArgumentException("검색 내용이 존재할 경우 검색 대상은 필수입니다.");
        }
    }

    private void checkQueryIsEmpty() {
        if (this.query.isEmpty()) {
            throw new KoinIllegalArgumentException("검색 내용의 최소 길이는 1입니다.");
        }
    }

    private void checkQueryIsBlank() {
        if (StringUtils.isBlank(this.query)) {
            throw new KoinIllegalArgumentException("검색 내용은 공백 문자로만 이루어져 있으면 안됩니다.");
        }
    }
}



