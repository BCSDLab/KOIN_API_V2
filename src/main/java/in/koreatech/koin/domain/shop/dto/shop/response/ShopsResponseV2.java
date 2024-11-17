package in.koreatech.koin.domain.shop.dto.shop.response;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import in.koreatech.koin.domain.shop.cache.dto.ShopCache;
import in.koreatech.koin.domain.shop.cache.dto.ShopCategoryCache;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteria;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteria;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfoV2;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopsResponseV2(
        @Schema(example = "100", description = "상점 개수", requiredMode = REQUIRED)
        Integer count,

        @Schema(description = "상점 정보")
        List<InnerShopResponse> shops
) {

    private static Predicate<ShopCache> queryPredicate(String query) {
        String trimmedQuery = query.replaceAll(" ", "");
        return (shop ->
                shop.name().contains(trimmedQuery) || shop.menuNames().stream().anyMatch(s -> s.contains(trimmedQuery))
        );
    }

    public static ShopsResponseV2 from(
            List<ShopCache> shops,
            Map<Integer, ShopInfoV2> shopInfoMap,
            ShopsSortCriteria sortBy,
            List<ShopsFilterCriteria> shopsFilterCriterias,
            LocalDateTime now,
            String query
    ) {
        List<InnerShopResponse> innerShopResponses = shops.stream()
                .filter(queryPredicate(query))
                .map(it -> {
                    ShopInfoV2 shopInfo = shopInfoMap.get(it.id());
                    return InnerShopResponse.from(
                            it,
                            shopInfo.durationEvent(),
                            it.isOpen(now),
                            shopInfo.averageRate(),
                            shopInfo.reviewCount()
                    );
                })
                .filter(ShopsFilterCriteria.createCombinedFilter(shopsFilterCriterias))
                .sorted(InnerShopResponse.getComparator(sortBy))
                .toList();
        return new ShopsResponseV2(
                innerShopResponses.size(),
                innerShopResponses
        );
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
                ShopCache shop,
                Boolean isEvent,
                Boolean isOpen,
                Double averageRate,
                Long reviewCount
        ) {
            return new InnerShopResponse(
                    shop.shopCategories().stream().map(ShopCategoryCache::id).toList(),
                    shop.delivery(),
                    shop.id(),
                    shop.name(),
                    shop.payBank(),
                    shop.payCard(),
                    shop.phone(),
                    isEvent,
                    isOpen,
                    averageRate,
                    reviewCount
            );
        }

        public static Comparator<InnerShopResponse> getComparator(ShopsSortCriteria shopsSortCriteria) {
            return Comparator
                    .comparing(InnerShopResponse::isOpen, Comparator.reverseOrder())
                    .thenComparing(shopsSortCriteria.getComparator());
        }
    }
}
