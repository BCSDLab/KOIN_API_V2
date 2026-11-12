package in.koreatech.koin.domain.shop.dto.shop.response;

import in.koreatech.koin.domain.language.model.SupportedLanguage;
import in.koreatech.koin.domain.language.service.OfficialNameResolver;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopSummaryResponse(
    @Schema(description = "상점 Id", example = "1", requiredMode = REQUIRED)
    Integer shopId,

    @Schema(description = "상점 이름", example = "수신반점", requiredMode = REQUIRED)
    String name,

    @Schema(description = "상점 소개", example = "안녕하세요 수신 반점입니다.", requiredMode = NOT_REQUIRED)
    String introduction,

    @Schema(description = "리뷰 평균 평점", example = "4.5", requiredMode = REQUIRED)
    double ratingAverage,

    @Schema(description = "리뷰 수", example = "15", requiredMode = REQUIRED)
    long reviewCount,

    @Schema(description = "이미지 url 리스트", requiredMode = REQUIRED)
    List<InnerImageResponse> images,

    @Schema(description = "선택 언어의 표시명. 번역이 없으면 원문을 사용합니다.")
    String displayName
) {
    public ShopSummaryResponse(
        Integer shopId,
        String name,
        String introduction,
        double ratingAverage,
        long reviewCount,
        List<InnerImageResponse> images
    ) {
        this(shopId, name, introduction, ratingAverage, reviewCount, images, name);
    }

    public ShopSummaryResponse localized(SupportedLanguage language) {
        return new ShopSummaryResponse(shopId, name, introduction, ratingAverage, reviewCount, images, OfficialNameResolver.shopName(name, language));
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerImageResponse(
        @Schema(description = "이미지 url", example = "https://static.koreatech.in/upload/market/2022/03/26/0e650fe1-811b-411e-9a82-0dd4f43c42ca-1648289492264.jpg", requiredMode = REQUIRED)
        String imageUrl,

        @Schema(description = "이미지 Thumbnail 여부", example = "true", requiredMode = REQUIRED)
        Boolean isThumbnail
    ) {
        public static InnerImageResponse from(ShopImage shopImage, boolean isThumbnail) {
            return new InnerImageResponse(
                shopImage.getImageUrl(),
                isThumbnail
            );
        }
    }

    public static ShopSummaryResponse from(Shop shop, ShopInfo shopInfo) {
        List<ShopImage> images = shop.getShopImages();

        List<InnerImageResponse> imageResponses = IntStream.range(0, images.size())
            .mapToObj(index -> InnerImageResponse.from(images.get(index), index == 0))
            .toList();

        return new ShopSummaryResponse(
            shop.getId(),
            shop.getName(),
            shop.getIntroduction(),
            shopInfo.averageRate(),
            shopInfo.reviewCount(),
            imageResponses
        );
    }
}
