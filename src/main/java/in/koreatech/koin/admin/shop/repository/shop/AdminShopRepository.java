package in.koreatech.koin.admin.shop.repository.shop;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.shop.exception.ShopNotFoundException;
import in.koreatech.koin.domain.shop.model.shop.Shop;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface AdminShopRepository extends Repository<Shop, Integer> {

    Shop save(Shop shop);

    Optional<Shop> findById(Integer shopId);

    List<Shop> findAllByIdIn(List<Integer> shopIds);

    @Query(value = "SELECT * FROM shops WHERE id = :shopId AND is_deleted = true", nativeQuery = true)
    Optional<Shop> findDeletedShopById(@Param("shopId") Integer shopId);

    @Query(value = "SELECT * FROM shops WHERE owner_id = :ownerId AND is_deleted = false", nativeQuery = true)
    List<Shop> findAllByOwnerId(@Param("ownerId") Integer ownerId);

    @Query(value = "SELECT * FROM shops WHERE is_deleted = :isDeleted",
        countQuery = "SELECT count(*) FROM shops WHERE is_deleted = :isDeleted",
        nativeQuery = true)
    Page<Shop> findAllByIsDeleted(@Param("isDeleted") boolean isDeleted, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM shops WHERE is_deleted = :isDeleted", nativeQuery = true)
    Integer countAllByIsDeleted(@Param("isDeleted") boolean isDeleted);

    @Query("SELECT s FROM Shop s WHERE s.name LIKE :searchKeyword%")
    List<Shop> searchByName(@Param("searchKeyword") String searchKeyword);

    @Modifying
    @Query(value = "UPDATE shops SET is_deleted = true WHERE id = :shopId", nativeQuery = true)
    int deleteById(@Param("shopId") Integer shopId);

    default Shop getById(Integer shopId) {
        return findById(shopId)
            .orElseThrow(() -> ShopNotFoundException.withDetail("shopId: " + shopId));
    }
}
