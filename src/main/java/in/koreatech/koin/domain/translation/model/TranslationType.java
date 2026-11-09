package in.koreatech.koin.domain.translation.model;

public enum TranslationType {
    ARTICLE_SUMMARY, LOST_ITEM, RECRUITMENT, LOST_ITEM_MESSAGE, RECRUITMENT_MESSAGE, DINING, SHOP, SHOP_MENU, SHOP_INFO, ORDERABLE_SHOP_MENU, SHOP_MENU_CATEGORY, ORDERABLE_SHOP_MENU_GROUP;

    public boolean isMessage() {
        return this == LOST_ITEM_MESSAGE || this == RECRUITMENT_MESSAGE;
    }
}
