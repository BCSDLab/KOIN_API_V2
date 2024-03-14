package in.koreatech.koin.domain.shop.dto;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import in.koreatech.koin.domain.shop.model.MenuCategory;
import in.koreatech.koin.domain.shop.model.Shop;
import in.koreatech.koin.domain.shop.model.ShopCategoryMap;
import in.koreatech.koin.domain.shop.model.ShopImage;
import in.koreatech.koin.domain.shop.model.ShopOpen;

@JsonNaming(value = SnakeCaseStrategy.class)
public record ShopResponse(
    String address,
    Boolean delivery,
    Long deliveryPrice,
    String description,
    Long id,
    List<String> imageUrls,
    List<InnerMenuCategory> menuCategories,
    String name,
    List<InnerShopOpen> open,
    Boolean payBank,
    Boolean payCard,
    String phone,
    List<InnerShopCategory> shopCategories,
    String updatedAt
) {

    public static ShopResponse from(Shop shop, List<ShopOpen> shopOpens, List<ShopImage> shopImages,
        List<ShopCategoryMap> shopCategoryMaps, List<MenuCategory> menuCategories) {

        List<InnerShopOpen> innerShopOpens = shopOpens.stream().map(shopOpen -> new InnerShopOpen(
            shopOpen.getDayOfWeek(),
            shopOpen.getClosed(),
            shopOpen.getOpenTime().toString(),
            shopOpen.getCloseTime().toString()
        )).toList();

        List<String> imageUrls = shopImages.stream().map(shopImage -> shopImage.getImageUrl()).toList();

        List<InnerShopCategory> innerShopCategories = shopCategoryMaps.stream()
            .map(shopCategoryMap -> new InnerShopCategory(
                shopCategoryMap.getShopCategory().getId(),
                shopCategoryMap.getShopCategory().getName()
            ))
            .toList();

        List<InnerMenuCategory> innerMenuCategories = menuCategories.stream()
            .map(menuCategory -> new InnerMenuCategory(
                menuCategory.getId(),
                menuCategory.getName()
            ))
            .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return new ShopResponse(
            shop.getAddress(),
            shop.getDelivery(),
            shop.getDeliveryPrice(),
            shop.getDescription(),
            shop.getId(),
            imageUrls,
            innerMenuCategories,
            shop.getName(),
            innerShopOpens,
            shop.getPayBank(),
            shop.getPayCard(),
            shop.getPhone(),
            innerShopCategories,
            shop.getUpdatedAt().format(formatter)
        );
    }

    private record InnerShopOpen(
        String dayOfWeek,
        Boolean closed,
        String openTime,
        String closeTime
    ) {
    }

    private record InnerShopCategory(
        Long id,
        String name
    ) {
    }

    private record InnerMenuCategory(
        Long id,
        String name
    ) {
    }
}
