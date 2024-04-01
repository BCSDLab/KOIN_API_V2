package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.dto.ShopResponse.InnerShopOpen;
import in.koreatech.koin.domain.shop.model.Shop;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopsResponse(
    @Schema(example = "100", description = "상점 개수")
    Integer count,

    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {
    public static ShopsResponse from(List<Shop> shops) {
        return new ShopsResponse(
            shops.size(),
            shops.stream().map(InnerShopResponse::from).toList()
        );
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    private record InnerShopResponse(
        @Schema(example = "0", description = " 속해있는 상점 카테고리들의 고유 id 리스트")
        List<Long> categoryIds,

        @Schema(example = "true", description = "배달 가능 여부")
        boolean delivery,

        @Schema(example = "1", description = "고유 id")
        Long id,

        @Schema(example = "수신반점", description = "이름")
        String name,

        @Schema(description = "요일별 휴무 여부 및 장사 시간")
        List<InnerShopOpen> open,

        @Schema(example = "true", description = "계좌 이체 가능 여부 ")
        boolean payBank,

        @Schema(example = "true", description = "카드 계산 가능 여부")
        boolean payCard,

        @Schema(example = "041-000-0000", description = "전화번호")
        String phone
    ) {
        public static InnerShopResponse from(Shop shop) {
            return new InnerShopResponse(
                shop.getShopCategories().stream().map(shopCategoryMap ->
                    shopCategoryMap.getShopCategory().getId()
                ).toList(),
                shop.getDelivery(),
                shop.getId(),
                shop.getName(),
                shop.getShopOpens().stream().map(InnerShopOpen::from).toList(),
                shop.getPayBank(),
                shop.getPayCard(),
                shop.getPhone()
            );
        }
    }
}
