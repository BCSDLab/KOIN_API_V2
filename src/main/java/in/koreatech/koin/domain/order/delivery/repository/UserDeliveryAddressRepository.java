package in.koreatech.koin.domain.order.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.config.repository.JpaRepositoryMarker;
import in.koreatech.koin.domain.order.delivery.model.UserDeliveryAddress;

@JpaRepositoryMarker
public interface UserDeliveryAddressRepository extends JpaRepository<UserDeliveryAddress, Integer> {

}
