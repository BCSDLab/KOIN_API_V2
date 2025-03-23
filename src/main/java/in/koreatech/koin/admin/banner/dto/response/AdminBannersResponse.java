package in.koreatech.koin.admin.banner.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.model.Banner;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminBannersResponse(
    @Schema(description = "조건에 해당하는 배너 수", example = "10", requiredMode = REQUIRED)
    Long totalCount,

    @Schema(description = "조건에 해당하는 배너 중 현재 페이지에서 조회된 수", example = "5", requiredMode = REQUIRED)
    Integer currentCount,

    @Schema(description = "조건에 해당하는 배너를 조회할 수 있는 최대 페이지", example = "2", requiredMode = REQUIRED)
    Integer totalPage,

    @Schema(description = "현재 페이지", example = "1", requiredMode = REQUIRED)
    Integer currentPage,

    @Schema(description = "배너 리스트", requiredMode = REQUIRED)
    List<InnerAdminBannerResponse> banners
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminBannerResponse(
        @Schema(description = "배너 ID", example = "1")
        Integer id,

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

        @Schema(description = "ios 리다이렉션 링크", example = "https://example.com/1000won")
        String iosRedirectLink,

        @Schema(description = "배너 활성화 여부", example = "true")
        Boolean isActive,

        @Schema(description = "배너 생성일", example = "25.03.23")
        @JsonFormat(pattern = "yy.MM.dd")
        LocalDate createAt
    ) {
        public static InnerAdminBannerResponse from(Banner banner) {
            return new InnerAdminBannerResponse(
                banner.getId(),
                banner.getBannerCategory().getName(),
                banner.getPriority(),
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getWebRedirectLink(),
                banner.getAndroidRedirectLink(),
                banner.getIosRedirectLink(),
                banner.getIsActive(),
                banner.getCreatedAt().toLocalDate()
            );
        }
    }

    public static AdminBannersResponse from(Page<Banner> banners) {
        return new AdminBannersResponse(
            banners.getTotalElements(),
            banners.getContent().size(),
            banners.getTotalPages(),
            banners.getNumber() + 1,
            banners.getContent().stream()
                .map(InnerAdminBannerResponse::from)
                .collect(Collectors.toList())
        );
    }
}
