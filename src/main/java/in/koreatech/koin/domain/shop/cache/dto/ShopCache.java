package in.koreatech.koin.domain.shop.cache.dto;

import in.koreatech.koin.domain.shop.model.menu.Menu;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.domain.shop.model.shop.ShopImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public record ShopCache(
        Integer id,
        String name,
        String internalName,
        String chosung,
        String phone,
        String address,
        String description,
        Boolean delivery,
        Integer deliveryPrice,
        boolean payCard,
        boolean payBank,
        boolean isDeleted,
        boolean isEvent,
        String remarks,
        Integer hit,
        List<ShopCategoryCache> shopCategories,
        List<ShopOpenCache> shopOpens,
        List<String> shopImages,
        List<EventArticleCache> eventArticles,
        List<ShopReviewCache> reviews,
        List<String> menuNames,
        String bank,
        String accountNumber
) {

    public static ShopCache from(
            Shop shop
    ) {
        return new ShopCache(
                shop.getId(),
                shop.getName(),
                shop.getInternalName(),
                shop.getChosung(),
                shop.getPhone(),
                shop.getAddress(),
                shop.getDescription(),
                shop.getDelivery(),
                shop.getDeliveryPrice(),
                shop.isPayCard(),
                shop.isPayBank(),
                shop.isDeleted(),
                shop.isEvent(),
                shop.getRemarks(),
                shop.getHit(),
                shop.getShopCategories().stream().map(ShopCategoryCache::from).toList(),
                shop.getShopOpens().stream().map(ShopOpenCache::from).toList(),
                shop.getShopImages().stream().map(ShopImage::getImageUrl).toList(),
                shop.getEventArticles().stream().map(EventArticleCache::from).toList(),
                shop.getReviews().stream().map(ShopReviewCache::from).toList(),
                shop.getMenus().stream().map(Menu::getName).toList(),
                shop.getBank(),
                shop.getAccountNumber()
        );
    }

    public boolean isOpen(LocalDateTime now) {
        String currDayOfWeek = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US).toUpperCase();
        String prevDayOfWeek = now.minusDays(1).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US).toUpperCase();
        for (ShopOpenCache shopOpen : shopOpens) {
            if (shopOpen.closed()) {
                continue;
            }
            if (shopOpen.dayOfWeek().equals(currDayOfWeek) &&
                    isBetweenDate(now, shopOpen, now.toLocalDate())
            ) {
                return true;
            }
            if (shopOpen.dayOfWeek().equals(prevDayOfWeek) &&
                    isBetweenDate(now, shopOpen, now.minusDays(1).toLocalDate())
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean isBetweenDate(LocalDateTime now, ShopOpenCache shopOpen, LocalDate criteriaDate) {
        LocalDateTime start = LocalDateTime.of(criteriaDate, shopOpen.openTime());
        LocalDateTime end = LocalDateTime.of(criteriaDate, shopOpen.closeTime());
        if (!shopOpen.closeTime().isAfter(shopOpen.openTime())) {
            end = end.plusDays(1);
        }
        return !start.isAfter(now) && !end.isBefore(now);
    }
}
