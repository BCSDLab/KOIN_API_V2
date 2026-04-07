package in.koreatech.koin.domain.order.delivery.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.order.delivery.model.UserDeliveryAddress;

public interface UserDeliveryAddressRepository extends Repository<UserDeliveryAddress, Integer> {

    UserDeliveryAddress save(UserDeliveryAddress entity);
}
