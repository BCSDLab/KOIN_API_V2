package in.koreatech.koin.domain.dining.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.dining.model.Dining;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;

public interface DiningRepository extends Repository<Dining, Long> {

    Dining save(Dining dining);

    List<Dining> findAllByDate(String date);

    Dining findById(Long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE dining_menus dm SET dm.sold_out = :soldOut WHERE dm.id = :menuId", nativeQuery = true)
    void updateSoldOut(@Param("menuId") Long menuId, @Param("soldOut") Boolean soldOut);
}
