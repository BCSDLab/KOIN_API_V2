package in.koreatech.koin.domain.ownershop.model;

import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.global.validation.SingleMenuPrice;
import in.koreatech.koin.global.validation.UniqueUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@JsonNaming(SnakeCaseStrategy.class)
public record ShopOrderServiceTransferData(
    @Schema(description = "이관할 상점 이미지 URL. 첫 번째 이미지가 대표 이미지입니다.")
    @NotNull @Size(max = 3) @UniqueUrl
    List<@NotBlank @Size(max = 255) String> imageUrls,

    @Schema(description = "이관할 카테고리별 메뉴. 기존 메뉴 조회 응답을 바탕으로 제출합니다.")
    @NotEmpty @Size(max = 100)
    List<@NotNull @Valid Category> menuCategories
) {

    public void validateFor(Shop shop) {
        Set<Integer> shopCategoryIds = shop.getMenuCategories().stream()
            .map(category -> category.getId()).collect(Collectors.toSet());
        Set<Integer> shopMenuIds = shop.getMenus().stream()
            .map(menu -> menu.getId()).collect(Collectors.toSet());
        Set<Integer> categoryIds = new HashSet<>();
        Map<Integer, Menu> menus = new HashMap<>();
        for (Category category : menuCategories) {
            if (!shopCategoryIds.contains(category.id()) || !categoryIds.add(category.id())) {
                throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT, "상점에 속하지 않거나 중복된 메뉴 카테고리입니다.");
            }
            Set<Integer> categoryMenuIds = new HashSet<>();
            for (Menu menu : category.menus()) {
                if (!shopMenuIds.contains(menu.id()) || !categoryMenuIds.add(menu.id())) {
                    throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT, "상점에 속하지 않거나 중복된 메뉴입니다.");
                }
                Menu previous = menus.putIfAbsent(menu.id(), menu);
                if (previous != null && !previous.equals(menu)) {
                    throw CustomException.of(ApiResponseCode.ILLEGAL_ARGUMENT, "여러 카테고리에 속한 메뉴의 내용은 같아야 합니다.");
                }
            }
        }
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record Category(
        @Schema(description = "기존 메뉴 카테고리 ID") @NotNull @Positive Integer id,
        @NotBlank @Size(max = 255) String name,
        @NotEmpty @Size(max = 500) List<@NotNull @Valid Menu> menus
    ) {
    }

    @SingleMenuPrice
    @JsonNaming(SnakeCaseStrategy.class)
    public record Menu(
        @Schema(description = "기존 메뉴 ID. 여러 카테고리에 속한 메뉴는 동일한 내용으로 제출합니다.")
        @NotNull @Positive Integer id,
        @NotBlank @Size(max = 25) String name,
        @Size(max = 80) String description,
        @NotNull Boolean isSingle,
        @PositiveOrZero Integer singlePrice,
        @Size(max = 100) List<@NotNull @Valid OptionPrice> optionPrices,
        @NotNull @Size(max = 3) @UniqueUrl List<@NotBlank @Size(max = 255) String> imageUrls
    ) {
    }

    @JsonNaming(SnakeCaseStrategy.class)
    public record OptionPrice(
        @NotBlank @Size(max = 50) String option,
        @NotNull @PositiveOrZero Integer price
    ) {
    }
}
