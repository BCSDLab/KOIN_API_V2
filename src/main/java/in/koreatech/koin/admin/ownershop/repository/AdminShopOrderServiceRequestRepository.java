package in.koreatech.koin.admin.ownershop.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;

public interface AdminShopOrderServiceRequestRepository extends Repository<ShopOrderServiceRequest, Integer> {

    Optional<ShopOrderServiceRequest> findById(Integer id);

    default ShopOrderServiceRequest getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST));
    }

    Long count();

    @Query("""
        SELECT COUNT(r) FROM ShopOrderServiceRequest r
        JOIN r.shop s
        WHERE s.name LIKE CONCAT('%', :shopName, '%')
        """)
    Long countByShopName(@Param("shopName") String shopName);

    @Query("""
        SELECT COUNT(r) FROM ShopOrderServiceRequest r
        WHERE r.requestStatus = :status
        """)
    Long countByStatus(@Param("status") ShopOrderServiceRequestStatus status);

    @Query("""
        SELECT COUNT(r) FROM ShopOrderServiceRequest r
        JOIN r.shop s
        WHERE r.requestStatus = :status
        AND s.name LIKE CONCAT('%', :shopName, '%')
        """)
    Long countByStatusAndShopName(
        @Param("status") ShopOrderServiceRequestStatus status,
        @Param("shopName") String shopName
    );

    @Query("""
        SELECT r FROM ShopOrderServiceRequest r
        JOIN FETCH r.shop s
        """)
    Page<ShopOrderServiceRequest> findPageOrderServiceRequests(Pageable pageable);

    @Query("""
        SELECT r FROM ShopOrderServiceRequest r
        JOIN FETCH r.shop s
        WHERE s.name LIKE CONCAT('%', :shopName, '%')
        """)
    Page<ShopOrderServiceRequest> findPageOrderServiceRequestsByShopName(@Param("shopName") String shopName,
        Pageable pageable);

    @Query("""
        SELECT r FROM ShopOrderServiceRequest r
        JOIN FETCH r.shop s
        WHERE r.requestStatus = :status
        """)
    Page<ShopOrderServiceRequest> findPageOrderServiceRequestsByStatus(
        @Param("status") ShopOrderServiceRequestStatus status, Pageable pageable);

    @Query("""
        SELECT r FROM ShopOrderServiceRequest r
        JOIN FETCH r.shop s
        WHERE r.requestStatus = :status
        AND s.name LIKE CONCAT('%', :shopName, '%')
        """)
    Page<ShopOrderServiceRequest> findPageOrderServiceRequestsByStatusAndShopName(
        @Param("status") ShopOrderServiceRequestStatus status,
        @Param("shopName") String shopName,
        Pageable pageable
    );
}
