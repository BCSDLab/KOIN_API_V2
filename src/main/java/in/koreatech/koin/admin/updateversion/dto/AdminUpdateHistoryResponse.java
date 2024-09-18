package in.koreatech.koin.admin.updateversion.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.updateversion.model.UpdateHistory;
import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminUpdateHistoryResponse(
    @Schema(description = "조건에 해당하는 총 버전 타입의 수", example = "2", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 버전 타입 중에 현재 페이지에서 조회된 수", example = "2", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 타입을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "버전 정보 리스트", requiredMode = REQUIRED)
    List<UpdateHistory> versions
) {

    public static AdminUpdateHistoryResponse of(Page<UpdateHistory> pagedResult, Criteria criteria) {
        return new AdminUpdateHistoryResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
        );
    }
}
