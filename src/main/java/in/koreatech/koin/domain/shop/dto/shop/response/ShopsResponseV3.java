package in.koreatech.koin.domain.shop.dto.shop.response;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.cache.dto.ShopCache;
import in.koreatech.koin.domain.shop.cache.dto.ShopCategoryCache;
import in.koreatech.koin.domain.shop.dto.shop.ShopsFilterCriteriaV3;
import in.koreatech.koin.domain.shop.dto.shop.ShopsSortCriteriaV3;
import in.koreatech.koin.domain.shop.model.shop.ShopOpen;
import in.koreatech.koin.domain.shop.repository.shop.dto.ShopInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopsResponseV3(
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

    public static ShopsResponseV3 from(
        List<ShopCache> shops,
        Map<Integer, ShopInfo> shopInfoMap,
        ShopsSortCriteriaV3 sortBy,
        List<ShopsFilterCriteriaV3> shopsFilterCriterias,
        LocalDateTime now,
        String query,
        Map<Integer, List<String>> benefitDetail,
        Map<Integer, List<String>> images,
        List<Integer> orderableShopIds
    ) {
        Set<Integer> orderableShopIdSet = new HashSet<>(orderableShopIds);
        List<InnerShopResponse> innerShopResponses = shops.stream()
            .filter(queryPredicate(query))
            .filter(shop -> !orderableShopIdSet.contains(shop.id()))
            .map(it -> {
                ShopInfo shopInfo = shopInfoMap.get(it.id());
                return InnerShopResponse.from(
                    it,
                    shopInfo.durationEvent(),
                    it.isOpen(now),
                    shopInfo.averageRate(),
                    shopInfo.reviewCount(),
                    benefitDetail.getOrDefault(it.id(), List.of()),
                    images.getOrDefault(it.id(), List.of())
                );
            })
            .filter(ShopsFilterCriteriaV3.createCombinedFilter(shopsFilterCriterias))
            .sorted(InnerShopResponse.getComparator(sortBy))
            .toList();
        return new ShopsResponseV3(
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

        @Schema(description = "요일별 휴무 여부 및 장사 시간")
        List<InnerShopOpen> open,

        @Schema(example = "true", description = "삭제 여부", requiredMode = REQUIRED)
        boolean isEvent,

        @Schema(example = "true", description = "운영중 여부", requiredMode = REQUIRED)
        boolean isOpen,

        @Schema(example = "4.9", description = "평균 별점", requiredMode = REQUIRED)
        double averageRate,

        @Schema(example = "10", description = "리뷰 개수", requiredMode = REQUIRED)
        long reviewCount,

        @Schema(example = "['배달비 무료', '콜라 서비스']", description = "혜택 설명", requiredMode = NOT_REQUIRED)
        List<String> benefitDetails,

        @Schema(description = "상점 이미지 목록", requiredMode = NOT_REQUIRED)
        List<String> images
    ) {

        @JsonNaming(value = SnakeCaseStrategy.class)
        public record InnerShopOpen(
            @Schema(example = "MONDAY", description = """
                        요일 = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
                        """, requiredMode = REQUIRED)
            String dayOfWeek,

            @Schema(example = "false", description = "휴무 여부", requiredMode = REQUIRED)
            Boolean closed,

            @JsonFormat(pattern = "HH:mm")
            @Schema(example = "02:00", description = "오픈 시간", requiredMode = NOT_REQUIRED)
            LocalTime openTime,

            @JsonFormat(pattern = "HH:mm")
            @Schema(example = "16:00", description = "마감 시간", requiredMode = NOT_REQUIRED)
            LocalTime closeTime
        ) {

            public static ShopResponse.InnerShopOpen from(ShopOpen shopOpen) {
                return new ShopResponse.InnerShopOpen(
                    shopOpen.getDayOfWeek(),
                    shopOpen.isClosed(),
                    shopOpen.getOpenTime(),
                    shopOpen.getCloseTime()
                );
            }
        }

        public static InnerShopResponse from(
            ShopCache shop,
            Boolean isEvent,
            Boolean isOpen,
            Double averageRate,
            Long reviewCount,
            List<String> benefitDetails,
            List<String> images
        ) {
            return new InnerShopResponse(
                shop.shopCategories().stream().map(ShopCategoryCache::id).toList(),
                shop.delivery(),
                shop.id(),
                shop.name(),
                shop.payBank(),
                shop.payCard(),
                shop.phone(),
                shop.shopOpens().stream().map(shopOpen ->
                    new InnerShopOpen(
                        shopOpen.dayOfWeek(),
                        shopOpen.closed(),
                        shopOpen.openTime(),
                        shopOpen.closeTime()
                    )
                ).toList(),
                isEvent,
                isOpen,
                averageRate,
                reviewCount,
                benefitDetails,
                images
            );
        }

        public static Comparator<InnerShopResponse> getComparator(ShopsSortCriteriaV3 shopsSortCriteria) {
            return Comparator
                .comparing(InnerShopResponse::isOpen, Comparator.reverseOrder())
                .thenComparing(shopsSortCriteria.getComparator());
        }
    }
}

