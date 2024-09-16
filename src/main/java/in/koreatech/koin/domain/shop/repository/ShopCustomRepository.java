package in.koreatech.koin.domain.shop.repository;

import java.time.LocalDate;

import in.koreatech.koin.domain.shop.dto.ShopsResponseV2;

public interface ShopCustomRepository {

    ShopsResponseV2 getShops(LocalDate now);
}
