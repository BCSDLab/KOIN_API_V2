package in.koreatech.koin.domain.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.model.ShopOpen;

public interface ShopOpenRepository extends Repository<ShopOpen, Integer> {

    @Query(value = """
        SELECT CASE
            WHEN COUNT(*) > 0 THEN 'true'
            ELSE 'false'
        END AS is_open
        FROM shop_opens s
        WHERE s.shop_id = :shopId
          AND (
            (
              s.day_of_week = UPPER(DAYNAME(CURDATE())) AND
              (
                (s.open_time <= CURRENT_TIME() AND s.close_time >= CURRENT_TIME())
                OR (s.open_time <= CURRENT_TIME() AND s.close_time = '00:00')
              )
            )
            OR
            (
              s.day_of_week = UPPER(DAYNAME(CURDATE() - INTERVAL 1 DAY)) AND
              (s.close_time <= s.open_time AND CURRENT_TIME() <= s.close_time)
            )
          )
    """, nativeQuery = true)
    boolean isOpen(@Param("shopId") Integer shopId);


    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Integer shopId);
}
