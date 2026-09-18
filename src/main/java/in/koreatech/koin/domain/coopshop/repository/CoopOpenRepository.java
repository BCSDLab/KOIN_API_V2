package in.koreatech.koin.domain.coopshop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.DayType;

public interface CoopOpenRepository extends Repository<CoopOpen, Integer> {

    Optional<CoopOpen> findByCoopShopAndTypeAndDayOfWeek(CoopShop coopShop, String type, DayType dayOfWeek);
}
