package in.koreatech.koin.admin.banner.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.model.BannerCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record AdminBannerCategoriesResponse(
    List<InnerAdminBannerCategoriesResponse> bannerCategories
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerAdminBannerCategoriesResponse(
        @Schema(description = "배너 카테고리 ID", example = "1")
        Integer id,

        @Schema(description = "배너 카테고리 이름", example = "메인 모달")
        String name,

        @Schema(description = "배너 카테고리 설명", example = "140*112 앱/웹 랜딩시 뜨는 모달입니다.")
        String description
    ) {
        public static InnerAdminBannerCategoriesResponse of(BannerCategory bannerCategory) {
            return new InnerAdminBannerCategoriesResponse(
                bannerCategory.getId(),
                bannerCategory.getName(),
                bannerCategory.getDescription()
            );
        }
    }

    public static AdminBannerCategoriesResponse of(List<BannerCategory> bannerCategories) {
        return new AdminBannerCategoriesResponse(bannerCategories.stream()
                .map(InnerAdminBannerCategoriesResponse::of)
                .collect(Collectors.toList())
        );
    }
}
