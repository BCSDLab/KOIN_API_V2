package in.koreatech.koin.admin.ownershop.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.common.model.Criteria;
import in.koreatech.koin.domain.owner.model.OwnerIncludingShop;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminShopOrderServiceResponse(
    @Schema(description = "조건에 해당하는 주문 요청", example = "57", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 현재 페이지에서 조회된 수", example = "10", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 최대 페이지", example = "6", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "2", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "주문 서비스 요청 리스트", requiredMode = REQUIRED)
    List<AdminShopOrderServiceResponse.InnerShopOrderServiceResponse> orderServiceRequests
) {
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record InnerShopOrderServiceResponse(
    ) {
        public static InnerShopOrderServiceResponse from(ShopOrderServiceRequest shopOrderServiceRequest) {
            return new InnerShopOrderServiceResponse(
            );
        }
    }

    public static AdminShopOrderServiceResponse of(
        List<ShopOrderServiceRequest> orderServiceRequests,
        Page<ShopOrderServiceRequest> page,
        Criteria criteria
    ) {
        return new AdminShopOrderServiceResponse(
            page.getTotalElements(),
            orderServiceRequests.size(),
            page.getTotalPages(),
            criteria.getPage(),
            orderServiceRequests.stream()
                .map(InnerShopOrderServiceResponse::from)
                .toList()
        );
    }
}
