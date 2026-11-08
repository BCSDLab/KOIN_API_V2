package in.koreatech.koin.admin.shop.repository.shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequest;
import in.koreatech.koin.domain.ownershop.model.ShopOrderServiceRequestStatus;

public interface AdminShopOrderServiceRequestRepository extends Repository<ShopOrderServiceRequest, Integer> {

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
