package in.koreatech.koin.admin.coopShop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.global.model.Criteria;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCoopShopsResponse(

    @Schema(description = "조건에 해당하는 총 생협 매장의 수", example = "20", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 생협 매장 중 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 생협 매장을 조회할 수 있는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "생협 매장 정보 리스트", requiredMode = REQUIRED)
    List<AdminCoopShopResponse> coopShops
) {
    public static AdminCoopShopsResponse of(Page<CoopShop> pagedResult, Criteria criteria) {
        return new AdminCoopShopsResponse(
            pagedResult.getTotalElements(),
            pagedResult.getContent().size(),
            pagedResult.getTotalPages(),
            criteria.getPage() + 1,
            pagedResult.getContent()
                .stream()
                .map(AdminCoopShopResponse::from)
                .toList()
        );
    }
}
