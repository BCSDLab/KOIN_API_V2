package in.koreatech.koin.domain.shop.dto.shop;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.dto.shop.ShopResponse.InnerShopOpen;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.review.ShopReview;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopsResponseV2(
    @Schema(example = "100", description = "상점 개수", requiredMode = REQUIRED)
    Integer count,

    @Schema(description = "상점 정보")
    List<InnerShopResponse> shops
) {

    public static ShopsResponseV2 from(
        List<InnerShopResponse> shops
    ) {
        return new ShopsResponseV2(shops.size(), shops);
    }

    @JsonNaming(value = SnakeCaseStrategy.class)
    public record InnerShopResponse(
        @Schema(example = "[1, 2, 3]", description = "속해있는 상점 카테고리들의 고유 id 리스트", requiredMode = NOT_REQUIRED)
        List<Integer> categoryIds,

        @Schema(example = "true", description = "배달 가능 여부", requiredMode = REQUIRED)
        boolean delivery,

        @Schema(example = "1", description = "고유 id", requiredMode = REQUIRED)
        Integer id,

        @Schema(example = "수신반점", description = "이름", requiredMode = REQUIRED)
        String name,

        @Schema(description = "요일별 휴무 여부 및 장사 시간")
        List<InnerShopOpen> open,

        @Schema(example = "true", description = "계좌 이체 가능 여부", requiredMode = REQUIRED)
        boolean payBank,

        @Schema(example = "true", description = "카드 계산 가능 여부", requiredMode = REQUIRED)
        boolean payCard,

        @Schema(example = "041-000-0000", description = "전화번호", requiredMode = NOT_REQUIRED)
        String phone,

        @Schema(example = "true", description = "삭제 여부", requiredMode = REQUIRED)
        boolean isEvent,

        @Schema(example = "true", description = "운영중 여부", requiredMode = REQUIRED)
        boolean isOpen,

        @Schema(example = "4.9", description = "평균 별점", requiredMode = REQUIRED)
        double averageRate,

        @Schema(example = "10", description = "리뷰 개수", requiredMode = REQUIRED)
        long reviewCount
    ) {

        public static InnerShopResponse from(
            Shop shop,
            boolean isEvent,
            boolean isOpen
        ) {
            return new InnerShopResponse(
                shop.getShopCategories().stream().map(shopCategoryMap ->
                    shopCategoryMap.getShopCategory().getId()
                ).toList(),
                shop.getDelivery(),
                shop.getId(),
                shop.getName(),
                shop.getShopOpens().stream().sorted().map(InnerShopOpen::from).toList(),
                shop.isPayBank(),
                shop.isPayCard(),
                shop.getPhone(),
                isEvent,
                isOpen,
                Math.round(shop.getReviews().stream()
                    .filter(review -> !review.isDeleted())
                    .mapToInt(ShopReview::getRating)
                    .average()
                    .orElse(0.0) * 10) / 10.0,
                shop.getReviews().stream()
                    .filter(review -> !review.isDeleted())
                    .count()
            );
        }

        public static Comparator<InnerShopResponse> getComparator(ShopsSortCriteria shopsSortCriteria) {
            return Comparator
                .comparing(InnerShopResponse::isOpen, Comparator.reverseOrder())
                .thenComparing(shopsSortCriteria.getComparator());
        }
    }
}
