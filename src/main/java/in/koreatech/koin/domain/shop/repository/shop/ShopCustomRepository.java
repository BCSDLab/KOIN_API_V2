package in.koreatech.koin.domain.shop.repository.shop;

import java.time.LocalDate;

import in.koreatech.koin.domain.benefit.dto.BenefitShopsResponse;

public interface ShopCustomRepository {

    BenefitShopsResponse getBenefitShops(Integer benefit, LocalDate now);
}
