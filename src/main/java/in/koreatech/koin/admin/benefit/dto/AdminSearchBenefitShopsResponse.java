package in.koreatech.koin.admin.benefit.dto;

import java.util.List;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminSearchBenefitShopsResponse(
    @Schema(description = "상점 리스트")
    List<InnerShopResponse> shops
) {

    public record InnerShopResponse(
        @Schema(description = "상점 ID", example = "1")
        Integer id,

        @Schema(description = "상점 이름", example = "수신반점")
        String name,

        @Schema(description = "혜택 여부", example = "true")
        boolean hasBenefit
    ) {

        public static AdminCreateBenefitShopsResponse.InnerShopResponse from(Shop shop) {
            return new AdminCreateBenefitShopsResponse.InnerShopResponse(
                shop.getId(),
                shop.getName()
            );
        }
    }
}
