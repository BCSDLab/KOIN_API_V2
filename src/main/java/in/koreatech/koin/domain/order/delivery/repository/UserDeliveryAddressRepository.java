package in.koreatech.koin.domain.order.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.order.delivery.model.UserDeliveryAddress;

@in.koreatech.koin.config.repository.JpaRepository
public interface UserDeliveryAddressRepository extends JpaRepository<UserDeliveryAddress, Integer> {

}
