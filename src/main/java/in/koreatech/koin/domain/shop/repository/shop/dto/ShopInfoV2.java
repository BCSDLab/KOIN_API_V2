package in.koreatech.koin.domain.shop.repository.shop.dto;

public record ShopInfoV2(
    Boolean durationEvent,
    Double averageRate,
    Long reviewCount,
    Boolean isOpen
) {

}
