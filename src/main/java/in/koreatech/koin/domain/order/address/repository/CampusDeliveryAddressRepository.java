package in.koreatech.koin.domain.order.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;

@in.koreatech.koin.config.repository.JpaRepository
public interface CampusDeliveryAddressRepository extends JpaRepository<CampusDeliveryAddress, Integer> {

    @Query("""
            SELECT cda
            FROM CampusDeliveryAddress cda
            JOIN FETCH cda.campusAddressType
        """)
    List<CampusDeliveryAddress> findAll();

    @Query("""
            SELECT cda
            FROM CampusDeliveryAddress cda
            JOIN FETCH cda.campusAddressType
            WHERE cda.campusAddressType.id = :campusAddressTypeId
        """)
    List<CampusDeliveryAddress> findByCampusAddressType_Id(Integer campusAddressTypeId);

    @Query("""
            SELECT cda
            FROM CampusDeliveryAddress cda
            JOIN FETCH cda.campusAddressType
            WHERE cda.id = :campusDeliveryAddressId
        """)
    Optional<CampusDeliveryAddress> findById(
        @Param("campusDeliveryAddressId") Integer campusDeliveryAddressId
    );

    default CampusDeliveryAddress getById(Integer campusDeliveryAddressId) {
        return findById(campusDeliveryAddressId).orElseThrow(
            () -> CustomException.of(ApiResponseCode.NOT_FOUND_DELIVERY_ADDRESS, "교내 배달 주소를 찾을 수 없습니다.")
        );
    }
}
