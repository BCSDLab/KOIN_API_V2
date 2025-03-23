package in.koreatech.koin.admin.banner.dto.request;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.model.Banner;
import in.koreatech.koin.domain.banner.model.BannerCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminBannerCreateRequest(
    @Schema(description = "배너 카테고리", example = "메인 모달", requiredMode = REQUIRED)
    @NotBlank(message = "배너 카테고리는 공백일 수 없습니다")
    @Size(max = 255, message = "배너 카테고리 이름은 최대 255자 입니다.")
    String bannerCategory,

    @Schema(description = "배너 제목", example = "천원의 아침", requiredMode = REQUIRED)
    @NotBlank(message = "배너 제목은 공백일 수 없습니다")
    @Size(max = 255, message = "배너 제목은 최대 255자 입니다.")
    String title,

    @Schema(description = "배너 이미지 링크", example = "https://example.com/1000won.jpg", requiredMode = REQUIRED)
    @NotBlank(message = "배너 이미지 링크는 공백일 수 없습니다")
    @Size(max = 255, message = "배너 이미지 링크는 최대 255자 입니다.")
    String imageUrl,

    @Schema(description = "웹 리다이렉션 링크", example = "https://example.com/1000won", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "웹 리다이렉션 링크는 최대 255자 입니다.")
    String webRedirectLink,

    @Schema(description = "안드로이드 리다이렉션 링크", example = "https://example.com/1000won", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "안드로이드 리다이렉션 링크는 최대 255자 입니다.")
    String androidRedirectLink,

    @Schema(description = "ios 리다이렉션 링크", example = "https://example.com/1000won", requiredMode = NOT_REQUIRED)
    @Size(max = 255, message = "ios 리다이렉션 링크는 최대 255자 입니다.")
    String iosRedirectLink
) {
    public Banner of(BannerCategory bannerCategory) {
        return Banner.builder()
            .bannerCategory(bannerCategory)
            .title(title)
            .imageUrl(imageUrl)
            .webRedirectLink(webRedirectLink)
            .androidRedirectLink(androidRedirectLink)
            .iosRedirectLink(iosRedirectLink)
            .isActive(false)
            .build();
    }
}
