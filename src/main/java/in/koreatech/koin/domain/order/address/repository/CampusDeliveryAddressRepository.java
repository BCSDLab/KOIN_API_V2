package in.koreatech.koin.domain.order.address.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
