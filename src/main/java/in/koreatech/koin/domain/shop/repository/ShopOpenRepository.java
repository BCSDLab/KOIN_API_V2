package in.koreatech.koin.domain.shop.repository;

import java.time.LocalDate;
import java.time.LocalTime;
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
              s.day_of_week = UPPER(DAYNAME(:nowDate)) AND
              (
                ((s.open_time <= :nowTime AND s.close_time >= :nowTime)
                OR (s.open_time <= :nowTime AND s.close_time = '00:00'))
                AND s.closed = false
              )
            )
            OR
            (
              (s.day_of_week = UPPER(DAYNAME(:nowDate - INTERVAL 1 DAY)) AND
              (s.close_time <= s.open_time AND :nowTime <= s.close_time))
              AND s.closed = false
            )
          )
    """, nativeQuery = true)
    boolean isOpen(
        @Param("shopId") Integer shopId,
        @Param("nowDate") LocalDate nowDate,
        @Param("nowTime") LocalTime nowTime
    );


    ShopOpen save(ShopOpen shopOpen);

    List<ShopOpen> findAllByShopId(Integer shopId);
}
