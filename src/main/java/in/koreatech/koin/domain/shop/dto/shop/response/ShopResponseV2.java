package in.koreatech.koin.domain.shop.dto.shop.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopResponseV2(
    @Schema(description = "상점 Id", example = "1", requiredMode = REQUIRED)
    Integer shopId,

    @Schema(description = "상점 이름", example = "수신반점", requiredMode = REQUIRED)
    String name,

    @Schema(description = "상점 소개", example = "안녕하세요 수신 반점입니다.", requiredMode = REQUIRED)
    String introduction,

    @Schema(description = "리뷰 평균 평점", example = "4.5", requiredMode = REQUIRED)
    double ratingAverage,

    @Schema(description = "리뷰 수", example = "15", requiredMode = REQUIRED)
    long reviewCount,

    @Schema(description = "이미지 url 리스트", requiredMode = REQUIRED)
    List<InnerImageResponse> images
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerImageResponse(
        @Schema(description = "이미지 url", example = "https://static.koreatech.in/upload/market/2022/03/26/0e650fe1-811b-411e-9a82-0dd4f43c42ca-1648289492264.jpg", requiredMode = REQUIRED)
        String imageUrl,

        @Schema(description = "이미지 Thumbnail 여부", example = "true", requiredMode = REQUIRED)
        Boolean isThumbnail
    ) {
        public static InnerImageResponse from(ShopImage shopImage) {
            return new InnerImageResponse(
                shopImage.getImageUrl(),
                false
            );
        }
    }

    public static ShopResponseV2 from(Shop shop, ShopInfo shopInfo) {
        return new ShopResponseV2(
            shop.getId(),
            shop.getName(),
            shop.getIntroduction(),
            shopInfo.averageRate(),
            shopInfo.reviewCount(),
            shop.getShopImages().stream()
                .map(InnerImageResponse::from)
                .toList()
        );
    }
}
