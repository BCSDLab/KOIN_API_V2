package in.koreatech.koin.domain.shop.repository.shop.dto;

public record ShopInfo(
    Boolean durationEvent,
    Double averageRate,
    Long reviewCount
) {
}
