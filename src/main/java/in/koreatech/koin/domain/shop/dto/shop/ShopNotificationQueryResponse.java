package in.koreatech.koin.domain.shop.dto.shop;

public record ShopNotificationQueryResponse(
    Integer shopId,
    String shopName,
    String notificationTitle,
    String notificationContent
) {

}
