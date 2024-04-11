package in.koreatech.koin.domain.ownershop;

public record ShopEventCreateEvent(
    Integer shopId,
    String shopName,
    String title
) {

}
