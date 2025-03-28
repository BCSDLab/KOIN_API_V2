package in.koreatech.koin.admin.banner.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.model.Banner;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminBannerResponse(
    @Schema(description = "배너 ID", example = "1")
    Integer id,

    @Schema(description = "배너 카테고리 id", example = "1")
    Integer bannerCategoryId,

    @Schema(description = "배너 카테고리", example = "메인 모달")
    String bannerCategory,

    @Schema(description = "배너 우선순위", example = "1")
    Integer priority,

    @Schema(description = "배너 제목", example = "천원의 아침")
    String title,

    @Schema(description = "배너 이미지 링크", example = "https://example.com/1000won.jpg")
    String imageUrl,

    @Schema(description = "웹 리다이렉션 링크", example = "https://example.com/1000won")
    String webRedirectLink,

    @Schema(description = "안드로이드 리다이렉션 링크", example = "https://example.com/1000won")
    String androidRedirectLink,

    @Schema(description = "안드로이드 최소 버전", example = "3.0.14")
    String androidMinimumVersion,

    @Schema(description = "ios 리다이렉션 링크", example = "https://example.com/1000won")
    String iosRedirectLink,

    @Schema(description = "ios 최소 버전", example = "3.0.14")
    String iosMinimumVersion,

    @Schema(description = "배너 활성화 여부", example = "true")
    Boolean isActive,

    @Schema(description = "배너 생성일", example = "25.03.23")
    @JsonFormat(pattern = "yy.MM.dd")
    LocalDate createdAt
) {
    public static AdminBannerResponse from(Banner banner) {
        return new AdminBannerResponse(
            banner.getId(),
            banner.getBannerCategory().getId(),
            banner.getBannerCategory().getName(),
            banner.getPriority(),
            banner.getTitle(),
            banner.getImageUrl(),
            banner.getWebRedirectLink(),
            banner.getAndroidRedirectLink(),
            banner.getAndroidMinimumVersion(),
            banner.getIosRedirectLink(),
            banner.getIosMinimumVersion(),
            banner.getIsActive(),
            banner.getCreatedAt().toLocalDate()
        );
    }
}
