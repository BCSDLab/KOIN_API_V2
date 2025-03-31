package in.koreatech.koin.domain.banner.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
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
        @Schema(description = "배너 ID", example = "1", requiredMode = REQUIRED)
        Integer id,

        @Schema(description = "배너 이름", example = "천원의 아침 식사", requiredMode = REQUIRED)
        String title,

        @Schema(description = "배너 이미지 링크", example = "https://example.com/1000won.jpg", requiredMode = REQUIRED)
        String imageUrl,

        @Schema(description = "플랫폼에 해당하는 리다이렉션 링크", example = "https://example.com/1000won", requiredMode = NOT_REQUIRED)
        String redirectLink,

        @Schema(description = "플랫폼에 해당하는 최소 버전", example = "3.0.14", requiredMode = NOT_REQUIRED)
        String version
    ) {

        public static InnerBannerResponse of(Banner banner, PlatformType platformType) {
            return new InnerBannerResponse(
                banner.getId(),
                banner.getTitle(),
                banner.getImageUrl(),
                resolveRedirectLink(banner, platformType),
                resolveVersion(banner, platformType)
            );
        }

        private static String resolveRedirectLink(Banner banner, PlatformType platformType) {
            return switch (platformType) {
                case WEB -> banner.getWebRedirectLink();
                case ANDROID -> banner.getAndroidRedirectLink();
                case IOS -> banner.getIosRedirectLink();
            };
        }

        private static String resolveVersion(Banner banner, PlatformType platformType) {
            return switch (platformType) {
                case ANDROID -> banner.getAndroidMinimumVersion();
                case IOS -> banner.getIosMinimumVersion();
                default -> null;
            };
        }
    }

    public static BannersResponse of(List<Banner> banners, PlatformType platformType) {
        List<InnerBannerResponse> innerBannerResponses = banners.stream()
            .filter(banner -> isNotReleasedPlatform(banner, platformType))
            .map(banner -> InnerBannerResponse.of(banner, platformType))
            .toList();
        return new BannersResponse(innerBannerResponses.size(), innerBannerResponses);
    }

    private static boolean isNotReleasedPlatform(Banner banner, PlatformType platformType) {
        return switch (platformType) {
            case ANDROID -> banner.getIsAndroidReleased();
            case IOS -> banner.getIsIosReleased();
            case WEB -> banner.getIsWebReleased();
        };
    }
}
