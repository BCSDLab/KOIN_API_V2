package in.koreatech.koin.domain.ownershop;

public record EventArticleCreateEvent(
    Integer shopId,
    String shopName,
    String title
) {

}
