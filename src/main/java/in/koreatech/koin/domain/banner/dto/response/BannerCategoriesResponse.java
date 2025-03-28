package in.koreatech.koin.domain.banner.dto.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.banner.model.BannerCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record BannerCategoriesResponse(
    List<InnerBannerCategoriesResponse> bannerCategories
) {
    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerBannerCategoriesResponse(
        @Schema(description = "배너 카테고리 ID", example = "1")
        Integer id,

        @Schema(description = "배너 카테고리 이름", example = "메인 모달")
        String name
    ) {
        public static InnerBannerCategoriesResponse of(BannerCategory bannerCategory) {
            return new InnerBannerCategoriesResponse(
                bannerCategory.getId(),
                bannerCategory.getName()
            );
        }
    }

    public static BannerCategoriesResponse of(List<BannerCategory> bannerCategories) {
        return new BannerCategoriesResponse(bannerCategories.stream()
            .map(InnerBannerCategoriesResponse::of)
            .collect(Collectors.toList())
        );
    }
}
