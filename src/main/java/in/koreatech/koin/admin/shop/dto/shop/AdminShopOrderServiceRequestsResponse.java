package in.koreatech.koin.admin.shop.dto.shop;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminShopOrderServiceRequestsResponse(
    @Schema(description = "전체 신청 수") Long totalCount,
    @Schema(description = "현재 페이지 신청 수") Integer currentCount,
    @Schema(description = "전체 페이지 수") Integer totalPage,
    @Schema(description = "현재 페이지 (1부터 시작)") Integer currentPage,
    @Schema(description = "신청 목록") List<InnerRequest> requests
) {

    public static AdminShopOrderServiceRequestsResponse from(Page<ShopOrderServiceRequest> page) {
        return new AdminShopOrderServiceRequestsResponse(
            page.getTotalElements(), page.getNumberOfElements(), page.getTotalPages(), page.getNumber() + 1,
            page.getContent().stream().map(InnerRequest::from).toList()
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record InnerRequest(
        @Schema(description = "신청 ID") Integer id,
        @Schema(description = "상점 ID") Integer shopId,
        @Schema(description = "상점명") String shopName,
        @Schema(description = "신청 일시") LocalDateTime createdAt
    ) {

        public static InnerRequest from(ShopOrderServiceRequest request) {
            return new InnerRequest(request.getId(), request.getShop().getId(), request.getShop().getName(),
                request.getCreatedAt());
        }
    }
}
