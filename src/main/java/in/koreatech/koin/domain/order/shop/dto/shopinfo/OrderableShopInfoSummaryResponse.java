package in.koreatech.koin.domain.order.shop.dto.shopinfo;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.order.shop.model.domain.OrderableShopInfoSummary;
import in.koreatech.koin.domain.order.shop.model.entity.shop.OrderableShopImage;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record OrderableShopInfoSummaryResponse(
    @Schema(description = "상점 ID", example = "14")
    Integer shopId,

    @Schema(description = "주문 가능 상점 ID", example = "1")
    Integer orderableShopId,

    @Schema(description = "상점 이름", example = "멕시카나 치킨 - 병천점")
    String name,

    @Schema(description = "상점 소개", example = "안녕하세요 멕시카나 치킨입니다.")
    String introduction,

    @Schema(description = "배달 가능 여부", example = "true")
    Boolean isDeliveryAvailable,

    @Schema(description = "포장 가능 여부", example = "false")
    Boolean isTakeoutAvailable,

    @Schema(description = "카드 결제 가능 여부", example = "false")
    Boolean payCard,

    @Schema(description = "계좌 이체 가능 여부", example = "false")
    Boolean payBank,

    @Schema(description = "최소 주문 금액", example = "15000")
    Integer minimumOrderAmount,

    @Schema(description = "리뷰 평균 평점", example = "4.5")
    Double ratingAverage,

    @Schema(description = "리뷰 수", example = "120")
    Integer reviewCount,

    @Schema(description = "최소 배달비", example = "0")
    Integer minimumDeliveryTip,

    @Schema(description = "최대 배달비", example = "3000")
    Integer maximumDeliveryTip,

    List<InnerImageResponse> images
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerImageResponse(
        @Schema(description = "이미지 url", example = "https://static.koreatech.in/upload/market/2022/03/26/0e650fe1-811b-411e-9a82-0dd4f43c42ca-1648289492264.jpg")
        String imageUrl,

        @Schema(description = "이미지 Thumbnail 여부", example = "true")
        Boolean isThumbnail
    ){

        public static InnerImageResponse from(OrderableShopImage orderableShopImage) {
            return new InnerImageResponse(orderableShopImage.getImageUrl(), orderableShopImage.getIsThumbnail());
        }

    }

    public static OrderableShopInfoSummaryResponse from(OrderableShopInfoSummary entity, List<OrderableShopImage> images) {
        return new OrderableShopInfoSummaryResponse(
            entity.shopId(),
            entity.orderableShopId(),
            entity.name(),
            entity.introduction(),
            entity.isDeliveryAvailable(),
            entity.isTakeoutAvailable(),
            entity.payCard(),
            entity.payBank(),
            entity.minimumOrderAmount(),
            entity.ratingAverage(),
            entity.reviewCount(),
            entity.minimumDeliveryTip(),
            entity.maximumDeliveryTip(),
            images.stream().map(InnerImageResponse::from).toList()
        );
    }
}

