package in.koreatech.koin.domain.shop.repository.shop;

import in.koreatech.koin.domain.shop.dto.shop.ShopNotificationQueryResponse;
import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends Repository<Shop, Integer> {

    Shop save(Shop shop);

    boolean existsById(Integer shopId);

    List<Shop> findAllByOwnerId(Integer ownerId);

    Optional<Shop> findById(Integer shopId);

    default Shop getById(Integer shopId) {
        return findById(shopId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("shopId: " + shopId));
    }

    @Query("""
        select s
        from Shop s
        left join fetch s.shopOperation op
        where s.isDeleted = false
    """)
    List<Shop> findAll();

    @Query("""
        SELECT s FROM Shop s
        JOIN FETCH s.eventArticles e
        LEFT JOIN FETCH s.shopOperation op
        LEFT JOIN FETCH s.shopMainCategory mc
        WHERE s.isDeleted = false AND e.isDeleted = false
    """)
    List<Shop> findAllWithEventArticles();

    @Query("""
        SELECT new in.koreatech.koin.domain.shop.dto.shop.ShopNotificationQueryResponse(
            s.id,
            s.name,
            n.title,
            n.content
        )
        FROM Shop s
        JOIN ShopCategoryMap scm ON s.id = scm.shop.id
        JOIN ShopCategory sc ON scm.shopCategory.id = sc.id
        JOIN ShopParentCategory spc ON sc.parentCategory.id = spc.id
        JOIN ShopNotificationMessage n ON spc.notificationMessage.id = n.id
        WHERE s.id IN :shopIds
        AND sc.id = (
            SELECT MIN(sc2.id)
            FROM ShopCategoryMap scm2
            JOIN ShopCategory sc2 ON scm2.shopCategory.id = sc2.id
            WHERE scm2.shop.id = s.id
            AND sc2.name != '전체보기'
        )
        """)
    List<ShopNotificationQueryResponse> findNotificationDataBatch(@Param("shopIds") List<Integer> shopIds);

    default Map<Integer, ShopNotificationQueryResponse> findNotificationDataBatchMap(List<Integer> shopIds) {
        return findNotificationDataBatch(shopIds).stream()
            .collect(Collectors.toMap(ShopNotificationQueryResponse::shopId, response -> response));
    }

    @Query("""
                SELECT s.id, 
                       CASE WHEN COUNT(e.id) > 0 THEN true ELSE false END 
                FROM Shop s
                LEFT JOIN s.eventArticles e
                ON e.startDate <= :now AND e.endDate >= :now
                GROUP BY s.id
            """)
    List<Object[]> findAllShopEventStatus(@Param("now") LocalDate now);

    default Map<Integer, Boolean> getAllShopEventDuration(LocalDate now) {
        return findAllShopEventStatus(now).stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (Boolean) result[1]
                ));
    }
}
