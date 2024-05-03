package in.koreatech.koin.domain.ownershop;

public record EventArticleCreateShopEvent(
    Integer shopId,
    String shopName,
    String title,
    String thumbnailImage
) {

}
