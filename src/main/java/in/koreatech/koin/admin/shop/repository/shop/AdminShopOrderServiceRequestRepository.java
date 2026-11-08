package in.koreatech.koin.admin.shop.repository.shop;

import java.util.Optional;

import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;

public interface AdminShopOrderServiceRequestRepository extends Repository<ShopOrderServiceRequest, Integer> {

    @Query("SELECT r FROM ShopOrderServiceRequest r JOIN FETCH r.shop WHERE r.id = :id")
    Optional<ShopOrderServiceRequest> findById(@Param("id") Integer id);

    default ShopOrderServiceRequest getById(Integer id) {
        return findById(id).orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_SHOP_ORDER_SERVICE_REQUEST));
    }

    @Query(value = """
        SELECT r FROM ShopOrderServiceRequest r
        JOIN FETCH r.shop s
        WHERE (:status IS NULL OR r.requestStatus = :status)
          AND (:keyword IS NULL OR s.name LIKE CONCAT('%', :keyword, '%'))
        """, countQuery = """
        SELECT COUNT(r) FROM ShopOrderServiceRequest r JOIN r.shop s
        WHERE (:status IS NULL OR r.requestStatus = :status)
          AND (:keyword IS NULL OR s.name LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<ShopOrderServiceRequest> findRequests(
        @Param("status") ShopOrderServiceRequestStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
