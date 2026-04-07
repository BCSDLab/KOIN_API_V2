package in.koreatech.koin.domain.order.delivery.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.order.delivery.model.RiderMessage;

public interface RiderMessageRepository extends Repository<RiderMessage, Integer> {

    List<RiderMessage> findAll();
}
