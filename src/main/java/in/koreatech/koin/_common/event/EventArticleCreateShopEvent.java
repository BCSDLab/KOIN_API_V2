package in.koreatech.koin._common.event;

public record EventArticleCreateShopEvent(
    Integer shopId,
    String shopName,
    String title,
    String thumbnailImage
) {

}
