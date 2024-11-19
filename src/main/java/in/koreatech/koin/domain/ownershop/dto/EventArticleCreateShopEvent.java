package in.koreatech.koin.domain.ownershop.dto;

public record EventArticleCreateShopEvent(
    Integer shopId,
    String shopName,
    String title,
    String thumbnailImage
) {

}
