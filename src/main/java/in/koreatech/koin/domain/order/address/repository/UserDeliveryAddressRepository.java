package in.koreatech.koin.domain.order.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.order.address.model.UserDeliveryAddress;

public interface UserDeliveryAddressRepository extends JpaRepository<UserDeliveryAddress, Integer> {

}
