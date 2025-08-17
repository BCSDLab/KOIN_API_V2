package in.koreatech.koin.common.event;

public record EventArticleCreateShopEvent(
    Integer shopId,
    String shopName,
    String title,
    String thumbnailImage
) {

}
