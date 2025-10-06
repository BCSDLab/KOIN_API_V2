package in.koreatech.koin.domain.coopshop.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.coopshop.exception.CoopOpenNotFoundException;
import in.koreatech.koin.domain.coopshop.model.CoopOpen;
import in.koreatech.koin.domain.coopshop.model.CoopShop;
import in.koreatech.koin.domain.coopshop.model.DayType;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface CoopOpenRepository extends Repository<CoopOpen, Integer> {

    Optional<CoopOpen> findByCoopShopAndTypeAndDayOfWeek(CoopShop coopShop, String type, DayType dayOfWeek);

    default CoopOpen getByCoopShopAndTypeAndDayOfWeek(CoopShop coopShop, String type, DayType dayOfWeek) {
        return findByCoopShopAndTypeAndDayOfWeek(coopShop, type, dayOfWeek)
            .orElseThrow(() -> CoopOpenNotFoundException.withDetail(
                String.format("coopShop: %s, type: %s, day of week: %s", coopShop, type, dayOfWeek.getDay())
            ));
    }
}
