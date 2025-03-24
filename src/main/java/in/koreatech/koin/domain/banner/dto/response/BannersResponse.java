package in.koreatech.koin.domain.banner.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.enums.PlatformType;
import in.koreatech.koin.domain.banner.model.Banner;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BannersResponse(
    @Schema(example = "10", description = "해당 카테고리의 활성화된 배너 개수", requiredMode = REQUIRED)
    Integer count,

    @Schema(description = "해당 카테고리의 활성화된 배너 정보 리스트")
    List<InnerBannerResponse> banners
) {

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerBannerResponse(
        @Schema(description = "배너 ID", example = "1")
        Integer id,

        @Schema(description = "배너 이미지 링크", example = "https://example.com/1000won.jpg")
        String imageUrl,

        @Schema(description = "플랫폼에 해당하는 리다이렉션 링크", example = "https://example.com/1000won")
        String redirectLink
    ) {

        public static InnerBannerResponse of(Banner banner, PlatformType platformType) {
            return new InnerBannerResponse(
                banner.getId(),
                banner.getImageUrl(),
                resolveRedirectLink(banner, platformType)
            );
        }

        private static String resolveRedirectLink(Banner banner, PlatformType platformType) {
            return switch (platformType) {
                case WEB -> banner.getWebRedirectLink();
                case ANDROID -> banner.getAndroidRedirectLink();
                case IOS -> banner.getIosRedirectLink();
            };
        }
    }

    public static BannersResponse of(List<Banner> banners, PlatformType platformType) {
        List<InnerBannerResponse> innerBannerResponses = banners.stream()
            .map(banner -> InnerBannerResponse.of(banner, platformType))
            .toList();
        return new BannersResponse(innerBannerResponses.size(), innerBannerResponses);
    }
}
