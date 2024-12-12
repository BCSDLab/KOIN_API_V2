package in.koreatech.koin.admin.benefit.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCreateBenefitShopsResponse(
    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {
    public static AdminCreateBenefitShopsResponse from(List<BenefitCategoryMap> benefitCategoryMaps) {
        return new AdminCreateBenefitShopsResponse(
            benefitCategoryMaps.stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    private record InnerShopResponse(
        @Schema(description = "상점 ID", example = "1")
        Integer id,

        @Schema(description = "상점 이름", example = "수신반점")
        String name,

        @Schema(example = "4인 이상 픽업서비스", description = "혜택 미리보기 문구")
        String detail
    ) {

        public static InnerShopResponse from(BenefitCategoryMap benefitCategoryMap) {
            return new InnerShopResponse(
                benefitCategoryMap.getShop().getId(),
                benefitCategoryMap.getShop().getName(),
                benefitCategoryMap.getDetail()
            );
        }
    }
}
