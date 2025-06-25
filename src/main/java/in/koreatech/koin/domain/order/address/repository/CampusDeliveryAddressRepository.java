package in.koreatech.koin.domain.order.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.order.address.exception.AddressErrorCode;
import in.koreatech.koin.domain.order.address.exception.AddressException;
import in.koreatech.koin.domain.order.address.model.CampusDeliveryAddress;

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
            () -> new AddressException(AddressErrorCode.CAMPUS_DELIVERY_ADDRESS_NOT_FOUND)
        );
    }
}
