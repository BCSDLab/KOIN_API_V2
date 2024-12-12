package in.koreatech.koin.admin.benefit.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.benefit.model.BenefitCategoryMap;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(SnakeCaseStrategy.class)
public record AdminBenefitShopsResponse(
    @Schema(example = "3", description = "상점 개수")
    Integer count,

    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {

    public static AdminBenefitShopsResponse from(List<BenefitCategoryMap> benefitCategoryMaps) {
        return new AdminBenefitShopsResponse(
            benefitCategoryMaps.size(),
            benefitCategoryMaps.stream()
                .map(InnerShopResponse::from)
                .toList()
        );
    }

    @JsonNaming(SnakeCaseStrategy.class)
    private record InnerShopResponse(
        @Schema(example = "1", description = "상점혜택 매핑id")
        Integer shopBenefitMapId,

        @Schema(example = "1", description = "고유 id")
        Integer id,

        @Schema(example = "수신반점", description = "이름")
        String name,

        @Schema(example = "4인 이상 픽업서비스", description = "혜택 미리보기 문구")
        String detail
    ) {

        public static InnerShopResponse from(BenefitCategoryMap benefitCategoryMap) {
            return new InnerShopResponse(
                benefitCategoryMap.getId(),
                benefitCategoryMap.getShop().getId(),
                benefitCategoryMap.getShop().getName(),
                benefitCategoryMap.getDetail()
            );
        }
    }
}
